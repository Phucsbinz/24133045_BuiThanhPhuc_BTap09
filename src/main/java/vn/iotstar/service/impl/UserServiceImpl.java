package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.service.OtpService;
import vn.iotstar.service.SessionRevocationService;
import vn.iotstar.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final SessionRevocationService sessionRevocationService;

    @Override
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng với id: " + id));
        return userMapper.toDto(user);
    }

    @Override
    public UserDTO findByUsername(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng với username: " + username));
        return userMapper.toDto(user);
    }

    @Override
    public UserDTO findByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng với email: " + email));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public Page<UserDTO> search(String keyword, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 1, 50);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<User> users = userRepository.searchUsers(keyword == null ? "" : keyword.trim(), pageable);
        if (users.isEmpty()) return users.map(userMapper::toDto);
        java.util.Map<Long, Long> counts = userRepository.countProductsForUsers(
                        users.getContent().stream().map(User::getId).toList()).stream()
                .collect(java.util.stream.Collectors.toMap(UserRepository.UserProductCount::getUserId,
                        UserRepository.UserProductCount::getProductCount));
        return users.map(user -> {
            UserDTO dto = userMapper.toDto(user);
            dto.setProductCount(counts.getOrDefault(user.getId(), 0L));
            return dto;
        });
    }

    @Override
    @Transactional
    public UserDTO create(UserDTO dto) {
        String username = clean(dto.getUsername());
        String email = clean(dto.getEmail()).toLowerCase(java.util.Locale.ROOT);
        if (username.contains("@")) throw new IllegalArgumentException("Username không được chứa ký tự @.");
        if (userRepository.existsByUsernameIgnoreCase(username)) throw new IllegalArgumentException("Username đã tồn tại.");
        if (userRepository.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("Email đã tồn tại.");
        String roleName = normalizeRole(dto.getRoleName());
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role không tồn tại."));
        User user = userMapper.toEntity(dto);
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(clean(dto.getFullName()));
        user.setRole(role);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setEnabled(dto.isEnabled());
        user.setEmailVerified(true);
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserDTO dto, Long actorId) {
        User user = userRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng."));
        String username = clean(dto.getUsername());
        String email = clean(dto.getEmail()).toLowerCase(java.util.Locale.ROOT);
        if (username.contains("@")) throw new IllegalArgumentException("Username không được chứa ký tự @.");
        userRepository.findByUsernameIgnoreCase(username).filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new IllegalArgumentException("Username đã tồn tại."); });
        userRepository.findByEmailIgnoreCase(email).filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new IllegalArgumentException("Email đã tồn tại."); });
        String roleName = normalizeRole(dto.getRoleName());
        boolean willBeAdmin = roleName.equals("ROLE_ADMIN") && dto.isEnabled();
        Role adminRole = roleRepository.findByNameForUpdate("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("Thiếu ROLE_ADMIN."));
        boolean removingEnabledAdmin = user.getRole().getId().equals(adminRole.getId())
                && user.isEnabled() && !willBeAdmin;
        if (removingEnabledAdmin && userRepository.countEnabledUsersByRoleId(adminRole.getId()) <= 1)
            throw new IllegalArgumentException("Không thể khóa hoặc hạ quyền ADMIN hoạt động cuối cùng.");
        if (actorId != null && actorId.equals(id) && !willBeAdmin)
            throw new IllegalArgumentException("Không thể tự khóa hoặc tự hạ quyền tài khoản hiện tại.");
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role không tồn tại."));
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(clean(dto.getFullName()));
        user.setRole(role);
        user.setEnabled(dto.isEnabled());
        User saved = userRepository.save(user);
        sessionRevocationService.revokeAfterCommit(id);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id, Long actorId) {
        User user = userRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng."));
        if (actorId != null && actorId.equals(id))
            throw new IllegalArgumentException("Không thể tự xóa tài khoản đang đăng nhập.");
        if (productRepository.countByUser_Id(id) > 0)
            throw new IllegalArgumentException("Tài khoản còn sản phẩm. Hãy xử lý sản phẩm trước khi xóa.");
        Role adminRole = roleRepository.findByNameForUpdate("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("Thiếu ROLE_ADMIN."));
        if (user.getRole().getId().equals(adminRole.getId()) && user.isEnabled()
                && userRepository.countEnabledUsersByRoleId(adminRole.getId()) <= 1)
            throw new IllegalArgumentException("Không thể xóa ADMIN hoạt động cuối cùng.");
        otpService.deleteByEmail(user.getEmail());
        userRepository.delete(user);
        sessionRevocationService.revokeAfterCommit(id);
    }

    @Override
    public long countUsers() { return userRepository.count(); }

    @Override
    public long countProducts(Long userId) { return productRepository.countByUser_Id(userId); }

    private static String clean(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin.");
        return value.trim();
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) return "ROLE_USER";
        String normalized = role.trim().toUpperCase(java.util.Locale.ROOT);
        if (normalized.equals("USER")) normalized = "ROLE_USER";
        if (normalized.equals("ADMIN")) normalized = "ROLE_ADMIN";
        if (!normalized.equals("ROLE_USER") && !normalized.equals("ROLE_ADMIN"))
            throw new IllegalArgumentException("Chỉ được chọn role USER hoặc ADMIN.");
        return normalized;
    }
}

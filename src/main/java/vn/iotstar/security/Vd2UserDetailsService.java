package vn.iotstar.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;

@Service("vd2UserDetailsService")
@RequiredArgsConstructor
public class Vd2UserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        if (login == null || login.trim().isEmpty()) {
            throw new UsernameNotFoundException("Tên đăng nhập hoặc email không được để trống.");
        }
        String cleanLogin = login.trim();
        User user = userRepository.findByUsernameOrEmailWithRole(cleanLogin)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản với username/email: " + cleanLogin));

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getFullName(),
                user.getImages(),
                user.getRole().getName(),
                user.isEnabled() && user.isEmailVerified()
        );
    }
}

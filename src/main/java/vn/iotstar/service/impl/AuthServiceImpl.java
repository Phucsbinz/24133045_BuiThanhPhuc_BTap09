package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.OtpService;
import vn.iotstar.service.SessionRevocationService;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final int OTP_MINUTES = 5;
    private static final int RESEND_SECONDS = 60;

    private final UserRepository users;
    private final RoleRepository roles;
    private final OtpTokenRepository otpTokens;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final SessionRevocationService sessions;

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        String username = dto.getUsername().trim();
        String email = normalizeEmail(dto.getEmail());
        if (username.contains("@")) throw new IllegalArgumentException("Username không được chứa ký tự @.");
        checkPassword(dto.getPassword());
        if (!dto.getPassword().equals(dto.getConfirmPassword()))
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        User existing = users.findByEmailIgnoreCase(email).orElse(null);
        if (existing != null && !existing.isEmailVerified()) {
            throw new IllegalArgumentException("Email này đang chờ xác nhận. Hãy gửi lại OTP.");
        }
        if (users.existsByUsernameIgnoreCase(username)) throw new IllegalArgumentException("Username đã tồn tại.");
        if (existing != null || users.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("Email đã tồn tại.");
        Role role = roles.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Database chưa có ROLE_USER."));
        User user = users.saveAndFlush(User.builder().username(username).email(email)
                .fullName(dto.getFullName().trim()).password(passwordEncoder.encode(dto.getPassword()))
                .role(role).enabled(true).emailVerified(false).build());
        otpService.issue(user.getEmail(), "REGISTER");
    }

    @Override
    @Transactional
    public boolean verifyRegistration(String emailInput, String otp) {
        String email = normalizeEmail(emailInput);
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản đang chờ xác nhận không tồn tại."));
        if (user.isEmailVerified()) return true;
        OtpToken token = lockedLatest(email, "REGISTER");
        if (!matchesAndConsume(token, otp)) return false;
        user.setEmailVerified(true);
        return true;
    }

    @Override
    @Transactional
    public void resendRegistrationOtp(String emailInput) {
        String email = normalizeEmail(emailInput);
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản cần xác nhận."));
        if (user.isEmailVerified()) throw new IllegalArgumentException("Email này đã được xác nhận.");
        enforceCooldown(email, "REGISTER");
        otpService.issue(email, "REGISTER");
    }

    @Override
    @Transactional
    public void requestPasswordReset(String emailInput) {
        String email = normalizeEmail(emailInput);
        User user = users.findByEmailIgnoreCase(email).orElse(null);
        if (user == null || !user.isEmailVerified()) return;
        enforceCooldown(email, "RESET_PASSWORD");
        otpService.issue(email, "RESET_PASSWORD");
    }

    @Override
    @Transactional
    public boolean resetPassword(String emailInput, String otp, String newPassword) {
        String email = normalizeEmail(emailInput);
        checkPassword(newPassword);
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Yêu cầu đặt lại mật khẩu không hợp lệ hoặc đã hết hạn."));
        if (!user.isEmailVerified()) return false;
        OtpToken token = lockedLatest(email, "RESET_PASSWORD");
        if (!matchesAndConsume(token, otp)) return false;
        user.setPassword(passwordEncoder.encode(newPassword));
        sessions.revokeAfterCommit(user.getId());
        return true;
    }

    private OtpToken lockedLatest(String email, String type) {
        return otpTokens.findUnusedForUpdate(email, type, PageRequest.of(0, 1)).stream().findFirst().orElse(null);
    }

    private boolean matchesAndConsume(OtpToken token, String rawOtp) {
        if (token == null || token.getExpiresAt().isBefore(LocalDateTime.now())
                || token.getAttempts() >= MAX_OTP_ATTEMPTS) return false;
        token.setAttempts(token.getAttempts() + 1);
        if (!passwordEncoder.matches(rawOtp, token.getOtpHash())) return false;
        token.setUsed(true);
        return true;
    }

    private void enforceCooldown(String email, String type) {
        otpTokens.findTopByEmailIgnoreCaseAndOtpTypeOrderByCreatedAtDesc(email, type).ifPresent(last -> {
            long seconds = Duration.between(last.getCreatedAt(), LocalDateTime.now()).getSeconds();
            if (seconds < RESEND_SECONDS)
                throw new IllegalArgumentException("Vui lòng đợi " + (RESEND_SECONDS - Math.max(seconds, 0)) + " giây trước khi gửi OTP mới.");
        });
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static void checkPassword(String password) {
        if (password == null || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Mật khẩu vượt quá giới hạn mã hóa an toàn.");
    }
}

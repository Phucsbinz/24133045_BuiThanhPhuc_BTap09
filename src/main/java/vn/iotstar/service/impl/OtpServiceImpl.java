package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.service.EmailService;
import vn.iotstar.service.OtpService;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private static final int OTP_MINUTES = 5;
    private final OtpTokenRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public void issue(String email, String type) {
        repository.deleteByEmailIgnoreCaseAndOtpType(email, type);
        String otp = "%06d".formatted(random.nextInt(1_000_000));
        repository.save(OtpToken.builder().email(email).otpHash(passwordEncoder.encode(otp)).otpType(type)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_MINUTES)).attempts(0).used(false)
                .createdAt(LocalDateTime.now()).build());
        emailService.sendOtp(email, otp, "IOTSTAR SHOP - Mã xác nhận tài khoản");
    }

    @Override
    @Transactional
    public void deleteByEmail(String email) {
        repository.deleteByEmailIgnoreCase(email);
    }
}

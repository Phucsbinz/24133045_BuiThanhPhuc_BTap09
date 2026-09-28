package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.iotstar.service.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Override
    public void sendOtp(String email, String otp, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText("""
                Mã xác nhận của bạn là: %s
                Mã có hiệu lực trong 5 phút, chỉ sử dụng một lần và tối đa 5 lần thử.
                Không chia sẻ mã này với người khác.
                """.formatted(otp));
        mailSender.send(message);
    }
}

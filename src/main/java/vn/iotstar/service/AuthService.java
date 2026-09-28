package vn.iotstar.service;

import vn.iotstar.dto.RegisterDTO;

public interface AuthService {
    void register(RegisterDTO dto);
    boolean verifyRegistration(String email, String otp);
    void resendRegistrationOtp(String email);
    void requestPasswordReset(String email);
    boolean resetPassword(String email, String otp, String password);
}

package vn.iotstar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VerifyOtpDTO {
    @NotBlank @Email
    private String email;

    @NotBlank(message = "Vui lòng nhập OTP")
    @Pattern(regexp = "\\d{6}", message = "OTP phải gồm 6 chữ số")
    private String otp;
}

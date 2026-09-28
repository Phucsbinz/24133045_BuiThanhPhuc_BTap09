package vn.iotstar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ResetPasswordDTO {
    @NotBlank @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Vui lòng nhập OTP")
    @Pattern(regexp = "\\d{6}", message = "OTP phải gồm 6 chữ số")
    private String otp;

    @NotBlank @Size(min = 6, max = 72, message = "Mật khẩu phải từ 6 đến 72 ký tự")
    private String password;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu")
    private String confirmPassword;
}

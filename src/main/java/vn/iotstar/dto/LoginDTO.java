package vn.iotstar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Tài khoản (email hoặc username) không được để trống")
    private String login;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}

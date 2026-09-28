package vn.iotstar.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String images;
    private Long roleId;
    private String roleName;
    private boolean enabled;
    private LocalDateTime createdAt;
}

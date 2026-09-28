package vn.iotstar.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("Initializing database roles and demo users...");

            // 1. Ensure Roles
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

            // 2. Demo Admin Account (VD1 teacher's email & admin access)
            if (!userRepository.existsByUsernameIgnoreCase("admin") && !userRepository.existsByEmailIgnoreCase("trungnh@hcmute.edu.vn")) {
                User admin = User.builder()
                        .username("admin")
                        .email("trungnh@hcmute.edu.vn")
                        .password(passwordEncoder.encode("123456"))
                        .fullName("ThS. Nguyễn Hữu Trung")
                        .images("/images/user.png")
                        .role(adminRole)
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(admin);
                log.info("Created demo ADMIN: admin / trungnh@hcmute.edu.vn (pass: 123456)");
            }

            // 3. Demo User 01 (VD2 standard sample user)
            if (!userRepository.existsByUsernameIgnoreCase("user01") && !userRepository.existsByEmailIgnoreCase("user01@gmail.com")) {
                User user01 = User.builder()
                        .username("user01")
                        .email("user01@gmail.com")
                        .password(passwordEncoder.encode("123456"))
                        .fullName("Nguyễn Hữu Trung")
                        .images("/images/user.png")
                        .role(userRole)
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(user01);
                log.info("Created demo USER: user01 / user01@gmail.com (pass: 123456)");
            }

            // 4. Demo User 02 (Student, no avatar -> tests fallback default avatar)
            if (!userRepository.existsByUsernameIgnoreCase("user02") && !userRepository.existsByEmailIgnoreCase("phuc.bui@example.com")) {
                User user02 = User.builder()
                        .username("user02")
                        .email("phuc.bui@example.com")
                        .password(passwordEncoder.encode("123456"))
                        .fullName("Bùi Thanh Phúc")
                        .images(null) // intentionally null to test fallback image
                        .role(userRole)
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(user02);
                log.info("Created demo USER (no avatar): user02 / phuc.bui@example.com (pass: 123456)");
            }

            // 5. Disabled User (tests login rejection for disabled accounts)
            if (!userRepository.existsByUsernameIgnoreCase("disabled_user") && !userRepository.existsByEmailIgnoreCase("locked@example.com")) {
                User disabledUser = User.builder()
                        .username("disabled_user")
                        .email("locked@example.com")
                        .password(passwordEncoder.encode("123456"))
                        .fullName("Tài Khoản Bị Khóa")
                        .images(null)
                        .role(userRole)
                        .enabled(false) // disabled
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(disabledUser);
                log.info("Created demo DISABLED USER: disabled_user / locked@example.com (pass: 123456)");
            }

            log.info("Database initialization completed successfully.");
        };
    }
}

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

            // Demo users are inserted only into a genuinely empty users table.
            // Restarting the application must never recreate an intentionally deleted account.
            if (userRepository.count() == 0) {
                userRepository.save(User.builder().username("admin").email("trungnh@hcmute.edu.vn")
                        .password(passwordEncoder.encode("123456")).fullName("ThS. Nguyễn Hữu Trung")
                        .images("/images/user.png").role(adminRole).enabled(true).emailVerified(true)
                        .createdAt(LocalDateTime.now()).build());
                userRepository.save(User.builder().username("user01").email("user01@gmail.com")
                        .password(passwordEncoder.encode("123456")).fullName("Nguyễn Hữu Trung")
                        .images("/images/user.png").role(userRole).enabled(true).emailVerified(true)
                        .createdAt(LocalDateTime.now()).build());
                userRepository.save(User.builder().username("user02").email("phuc.bui@example.com")
                        .password(passwordEncoder.encode("123456")).fullName("Bùi Thanh Phúc")
                        .images(null).role(userRole).enabled(true).emailVerified(true)
                        .createdAt(LocalDateTime.now()).build());
                userRepository.save(User.builder().username("disabled_user").email("locked@example.com")
                        .password(passwordEncoder.encode("123456")).fullName("Tài Khoản Bị Khóa")
                        .images(null).role(userRole).enabled(false).emailVerified(true)
                        .createdAt(LocalDateTime.now()).build());
                log.info("Created initial demo accounts for an empty database.");
            }

            log.info("Database initialization completed successfully.");
        };
    }
}

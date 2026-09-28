package vn.iotstar.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain vd1SecurityFilterChain(
            HttpSecurity http,
            @Qualifier("vd1UserDetailsService") UserDetailsService vd1UserDetailsService,
            PasswordEncoder passwordEncoder) throws Exception {

        DaoAuthenticationProvider vd1Provider = new DaoAuthenticationProvider(vd1UserDetailsService);
        vd1Provider.setPasswordEncoder(passwordEncoder);

        http
                .securityMatcher("/vd1/**")
                .authenticationProvider(vd1Provider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/vd1/login", "/vd1/login/**").permitAll()
                        .requestMatchers("/vd1/admin", "/vd1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/vd1/home", "/vd1/**").authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/vd1/login")
                        .loginProcessingUrl("/vd1/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/vd1/home", true)
                        .failureUrl("/vd1/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/vd1/logout")
                        .logoutSuccessUrl("/vd1/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain vd2SecurityFilterChain(
            HttpSecurity http,
            @Qualifier("vd2UserDetailsService") UserDetailsService vd2UserDetailsService,
            PasswordEncoder passwordEncoder) throws Exception {

        DaoAuthenticationProvider vd2Provider = new DaoAuthenticationProvider(vd2UserDetailsService);
        vd2Provider.setPasswordEncoder(passwordEncoder);

        http
                .securityMatcher("/vd2/**")
                .authenticationProvider(vd2Provider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/vd2/login", "/vd2/login/**").permitAll()
                        .requestMatchers("/vd2/admin", "/vd2/admin/**").hasRole("ADMIN")
                        .requestMatchers("/vd2/home", "/vd2/**").authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/vd2/login")
                        .loginProcessingUrl("/vd2/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/vd2/home", true)
                        .failureUrl("/vd2/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/vd2/logout")
                        .logoutSuccessUrl("/vd2/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                );

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/access-denied", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                );

        return http.build();
    }
}

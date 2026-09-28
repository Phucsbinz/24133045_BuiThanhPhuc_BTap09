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
import org.springframework.security.core.session.SessionRegistry;
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
    public SessionRegistry sessionRegistry() {
        return new org.springframework.security.core.session.SessionRegistryImpl();
    }

    @Bean
    public org.springframework.security.web.session.HttpSessionEventPublisher httpSessionEventPublisher() {
        return new org.springframework.security.web.session.HttpSessionEventPublisher();
    }

    private static void sharedSessions(HttpSecurity http, SessionRegistry registry, String expiredUrl)
            throws Exception {
        http.sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .sessionRegistry(registry)
                .expiredUrl(expiredUrl));
    }

    @Bean
    @Order(1)
    public SecurityFilterChain vd1SecurityFilterChain(
            HttpSecurity http,
            @Qualifier("vd1UserDetailsService") UserDetailsService vd1UserDetailsService,
            PasswordEncoder passwordEncoder,
            SessionRegistry sessionRegistry) throws Exception {

        DaoAuthenticationProvider vd1Provider = new DaoAuthenticationProvider(vd1UserDetailsService);
        vd1Provider.setPasswordEncoder(passwordEncoder);

        http
                .securityMatcher("/vd1/**")
                .authenticationProvider(vd1Provider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/vd1/login", "/vd1/login/**").permitAll()
                        .requestMatchers("/vd1/admin", "/vd1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/vd1/**").authenticated()
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
                        .deleteCookies("BTAP09_VD12_SESSION")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                );

        sharedSessions(http, sessionRegistry, "/vd1/login?expired=true");

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain vd2SecurityFilterChain(
            HttpSecurity http,
            @Qualifier("vd2UserDetailsService") UserDetailsService vd2UserDetailsService,
            PasswordEncoder passwordEncoder,
            SessionRegistry sessionRegistry) throws Exception {

        DaoAuthenticationProvider vd2Provider = new DaoAuthenticationProvider(vd2UserDetailsService);
        vd2Provider.setPasswordEncoder(passwordEncoder);

        http
                .securityMatcher("/vd2/**")
                .authenticationProvider(vd2Provider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/vd2/login", "/vd2/login/**").permitAll()
                        .requestMatchers("/vd2/admin", "/vd2/admin/**").hasRole("ADMIN")
                        .requestMatchers("/vd2/**").authenticated()
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
                        .deleteCookies("BTAP09_VD12_SESSION")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                );

        sharedSessions(http, sessionRegistry, "/vd2/login?expired=true");

        return http.build();
    }

    @Bean
    @Order(4)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {
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

    @Bean
    @Order(3)
    public SecurityFilterChain vd3SecurityFilterChain(
            HttpSecurity http,
            @Qualifier("vd2UserDetailsService") UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            SessionRegistry sessionRegistry) throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        http.securityMatcher("/vd3/**")
                .authenticationProvider(provider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/vd3/login", "/vd3/register", "/vd3/verify-otp",
                                "/vd3/resend-register-otp", "/vd3/forgot-password", "/vd3/reset-password")
                        .permitAll()
                        .requestMatchers("/vd3/users/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/vd3/login").loginProcessingUrl("/vd3/login")
                        .usernameParameter("username").passwordParameter("password")
                        .defaultSuccessUrl("/vd3/home", true).failureUrl("/vd3/login?error=true").permitAll())
                .logout(logout -> logout.logoutUrl("/vd3/logout")
                        .logoutSuccessUrl("/vd3/login?logout=true").invalidateHttpSession(true)
                        .deleteCookies("BTAP09_VD12_SESSION").permitAll())
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"));
        sharedSessions(http, sessionRegistry, "/vd3/login?expired=true");
        return http.build();
    }
}

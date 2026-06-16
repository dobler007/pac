package com.example.mas_implementation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Central Spring Security configuration for SportRadar.
 *
 * <ul>
 *   <li>Passwords are hashed with BCrypt.</li>
 *   <li>Public pages (home, browse games, events, locations, login, register, static assets)
 *       are open to guests; every state-changing request requires authentication.</li>
 *   <li>The {@code /admin/**} area requires ROLE_ADMIN.</li>
 *   <li>Form login uses the custom {@code /login} page; the username field is named
 *       {@code login} to match the existing template.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;

    public SecurityConfig(LoginSuccessHandler loginSuccessHandler) {
        this.loginSuccessHandler = loginSuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**",
                                 "/error", "/h2-console/**").permitAll()
                .requestMatchers("/login", "/register").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/profile/**").authenticated()
                // Any other GET is a public, read-only page (browse games, events, profiles).
                .requestMatchers(HttpMethod.GET, "/**").permitAll()
                // Everything else (join, resign, create, rate, review, ...) requires login.
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("login")
                .passwordParameter("password")
                .successHandler(loginSuccessHandler)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .permitAll()
            );

        // The H2 console serves its own frames and posts without a CSRF token.
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}

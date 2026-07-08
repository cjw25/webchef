package com.example.fivechef.WebChef.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 개발 중 403 방지용.
                // 배포 전에 CSRF를 다시 켤 거면 chatbot-widget.js의 CSRF 헤더 코드는 그대로 유지하면 됨.
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index",
                                "/user/login",
                                "/user/create",
                                "/user/find-id",
                                "/user/find-password",
                                "/fridge/**",
                                "/course/list",
                                "/course/detail/**",
                                "/community/list",
                                "/community/view/**",
                                "/notice/list",
                                "/notice/view/**",
                                "/inquiry/**",
                                "/tips/**",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/uploads/**",
                                "/assets/**",
                                "/api/chat/**",
                                "/api/users/register"
                        ).permitAll()

                        .requestMatchers("/mypage/**").authenticated()

                        .requestMatchers(
                                "/instructor/create",
                                "/instructor/create/**",
                                "/instructor/status",
                                "/instructor/status/**"
                        ).authenticated()

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .requestMatchers("/instructor/**").hasAnyRole("INSTRUCTOR", "ADMIN")

                        .anyRequest().authenticated()
                )

                .formLogin(login -> login
                        .loginPage("/user/login")
                        .loginProcessingUrl("/user/login")
                        .defaultSuccessUrl("/", true)
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}
package com.example.fivechef.WebChef.config;

import com.example.fivechef.WebChef.service.UserService;
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
                // 개발 중 403 방지용. 나중에 배포 전에 CSRF 다시 켜도 됨.
                .csrf(AbstractHttpConfigurer::disable)


                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index",
                                "/user/login",
                                "/user/create",
                                "/user/find-id",
                                "/user/find-password",
                                "/course/list",
                                "/course/detail/**",
                                "/community/list",
                                "/community/view/**",
                                "/notice/list",
                                "/notice/view/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/assets/**",
                                "/api/users/register"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .formLogin(login -> login
                        .loginPage("/user/login")
                        .defaultSuccessUrl("/", true)
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
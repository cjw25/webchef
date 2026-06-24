package com.example.fivechef.WebChef.config;

import com.example.fivechef.WebChef.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .userDetailsService(userService)

                .authorizeHttpRequests(auth -> auth

                        // 정적 파일
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()

                        // 전체 접근 가능
                        .requestMatchers(
                                "/",
                                "/index",
                                "/user/login",
                                "/user/loginProc",
                                "/user/create",
                                "/user/find-id",
                                "/user/find-password",
                                "/api/users/register",
                                "/api/user/find-id",
                                "/api/user/reset-password"
                        ).permitAll()

                        // 관리자 권한
                        .requestMatchers(
                                "/admin/**",
                                "/user/list",
                                "/user/view/**",
                                "/user/update/**",
                                "/user/delete/**",
                                "/user/role/**",
                                "/api/users/**"
                        ).hasRole("ADMIN")

                        // 강사 또는 관리자 권한
                        .requestMatchers(
                                "/instructor/**",
                                "/course/create",
                                "/course/update/**",
                                "/lesson/create",
                                "/lesson/update/**"
                        ).hasAnyRole("INSTRUCTOR", "ADMIN")

                        // 로그인 사용자
                        .requestMatchers(
                                "/mypage/**",
                                "/payment/**",
                                "/enrollment/**",
                                "/progress/**",
                                "/chatbot",
                                "/api/chatbot/**",
                                "/community/write",
                                "/community/comment/**"
                        ).authenticated()

                        .anyRequest().permitAll()
                )

                .formLogin(form -> form
                        .loginPage("/user/login")
                        .loginProcessingUrl("/user/loginProc")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/user/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/user/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
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
                // 개발 중 403 방지용
                .csrf(AbstractHttpConfigurer::disable)

                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )

                .authorizeHttpRequests(auth -> auth

                        /*
                         * 전체 공개 페이지
                         */
                        .requestMatchers(
                                "/",
                                "/index",

                                "/user/login",
                                "/user/create",
                                "/user/find-id",
                                "/user/find-password",

                                "/fridge/**",

                                // 강의 목록은 비로그인도 볼 수 있음
                                "/course/list",

                                "/community/list",
                                "/community/view/**",

                                "/metaverse",
                                "/unity/**",

                                "/notice/list",
                                "/notice/view/**",

                                "/payment/success",
                                "/payment/fail",

                                "/inquiry/**",
                                "/tips/**",

                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/images/**",
                                "/uploads/**",
                                "/assets/**",

                                "/api/chat/**",
                                "/api/users/register"
                        ).permitAll()

                        /*
                         * 마이페이지
                         */
                        .requestMatchers("/mypage/**").authenticated()

                        /*
                         * 강의 수강 / 상세
                         *
                         * 비로그인 사용자는 /course/list에서 강의를 눌러도
                         * CourseService가 /user/login으로 보내지만,
                         * 주소창에 /course/view/{id}를 직접 입력하는 것도 막기 위해 authenticated 처리
                         */
                        .requestMatchers(
                                "/course/view/**",
                                "/course/detail/**"
                        ).authenticated()

                        /*
                         * 강의 등록 / 수정 / 삭제
                         *
                         * USER는 불가
                         * INSTRUCTOR, ADMIN만 가능
                         */
                        .requestMatchers(
                                "/course/create",
                                "/course/create/**",
                                "/course/update/**",
                                "/course/delete/**"
                        ).hasAnyRole("INSTRUCTOR", "ADMIN")

                        /*
                         * 결제 페이지 / 결제 API
                         */
                        .requestMatchers(
                                "/payment/course/**",
                                "/api/payments/**"
                        ).authenticated()

                        /*
                         * 강사 신청은 로그인 사용자 가능
                         */
                        .requestMatchers(
                                "/instructor/create",
                                "/instructor/create/**",
                                "/instructor/status",
                                "/instructor/status/**"
                        ).authenticated()

                        /*
                         * 관리자
                         */
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .requestMatchers("/mypage/**").authenticated()

                        .requestMatchers("/subscription/**").hasRole("USER")

                        /*
                         * 강사 전용 페이지
                         */
                        .requestMatchers("/instructor/**").hasAnyRole("INSTRUCTOR", "ADMIN")

                        .requestMatchers("/instructor/courses/**").hasRole("INSTRUCTOR")

                        /*
                         * 나머지는 로그인 필요
                         */
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
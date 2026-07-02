package com.example.fivechef.WebChef.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        // 로그인 아이디
        @Column(nullable = false, unique = true, length = 50)
        private String username;

        // 암호화된 비밀번호
        @Column(nullable = false, length = 255)
        private String password;

        // 이름
        @Column(nullable = false, length = 50)
        private String name;

        // 이메일
        @Column(nullable = false, unique = true, length = 100)
        private String email;

        // 권한
        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        private Role role = Role.USER;

        // 계정 활성화 여부
        @Column(nullable = false)
        private Boolean active = true;

        // 생성일
        private LocalDateTime createdAt;

        // 수정일
        private LocalDateTime updatedAt;

        @PrePersist
        public void prePersist() {
                this.createdAt = LocalDateTime.now();
                this.updatedAt = LocalDateTime.now();

                if (this.role == null) {
                        this.role = Role.USER;
                }

                if (this.active == null) {
                        this.active = true;
                }
        }

        @PreUpdate
        public void preUpdate() {
                this.updatedAt = LocalDateTime.now();
        }
}
//package com.example.fivechef.WebChef.user.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "users")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class User {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long userId;
//
//    @Column(nullable = false, unique = true, length = 50)
//    private String loginId;
//
//    @Column(nullable = false, length = 255)
//    private String password;
//
//    @Column(nullable = false, length = 50)
//    private String name;
//
//    @Column(nullable = false, unique = true, length = 100)
//    private String email;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    private UserRole role;
//
//    @Column(nullable = false)
//    private Boolean isActive;
//
//    private LocalDateTime createdAt;
//
//    private LocalDateTime updatedAt;
//
//    @PrePersist
//    public void onCreate() {
//        this.createdAt = LocalDateTime.now();
//        this.updatedAt = LocalDateTime.now();
//
//        if (this.role == null) {
//            this.role = UserRole.STUDENT;
//        }
//
//        if (this.isActive == null) {
//            this.isActive = true;
//        }
//    }
//
//    @PreUpdate
//    public void onUpdate() {
//        this.updatedAt = LocalDateTime.now();
//    }
//}
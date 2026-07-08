package com.example.fivechef.WebChef.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "instructors",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_instructors_user_id", columnNames = "user_id")
        }
)
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 강사 신청한 사용자
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 전문 분야
    @Column(nullable = false, length = 100)
    private String specialty;

    // 자기소개
    @Column(columnDefinition = "TEXT", nullable = false)
    private String introduction;

    // 경력 / 경험
    @Column(columnDefinition = "TEXT")
    private String career;

    // 포트폴리오 URL
    private String portfolioUrl;

    // 신청 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InstructorStatus status = InstructorStatus.PENDING;

    // 반려 사유
    @Column(columnDefinition = "TEXT")
    private String rejectReason;

    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = InstructorStatus.PENDING;
        }
    }
}
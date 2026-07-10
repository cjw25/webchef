package com.example.fivechef.WebChef.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 강의 제목
    @Column(nullable = false, length = 200)
    private String title;

    // 강의 설명
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    // 썸네일 이미지 주소
    @Column(length = 500)
    private String thumbnailUrl;

    // 가격
    @Column(nullable = false)
    private Integer price = 0;

    // 가격 기준으로 자동 결정되는 구독권
    // 0원: null
    // 1원 ~ 200,000원: BASIC
    // 200,001원 이상: PREMIUM
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SubscriptionPlanType requiredPlanType;

    // 카테고리
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourseCategory category;

    // 난이도
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Difficulty difficulty;

    // 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourseStatus status = CourseStatus.DRAFT;

    // 강사
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.price == null) {
            this.price = 0;
        }

        if (this.status == null) {
            this.status = CourseStatus.DRAFT;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
package com.example.fivechef.WebChef.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 강의명
    @Column(nullable = false, length = 100)
    private String title;

    // 강의 요약
    @Column(nullable = false, length = 300)
    private String summary;

    // 강의 상세 설명
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    // 썸네일 이미지 경로
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    // 가격
    @Column(nullable = false)
    private int price;

    // 무료 여부
    @Column(name = "is_free", nullable = false)
    private boolean free;

    // 카테고리
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourseCategory category;

    // 난이도
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourseDifficulty difficulty;

    // 강의 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourseStatus status;

    // 강사 ID
    @Column(name = "instructor_id")
    private Long instructorId;

    // 강사명
    @Column(name = "instructor_name", length = 50)
    private String instructorName;

    // 조회수
    @Column(name = "view_count", nullable = false)
    private int viewCount;

    // 수강생 수
    @Column(name = "student_count", nullable = false)
    private int studentCount;

    // 생성일
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 수정일
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = CourseStatus.DRAFT;
        }

        if (this.free) {
            this.price = 0;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();

        if (this.free) {
            this.price = 0;
        }
    }

    public enum CourseCategory {
        KOREAN("한식"),
        CHINESE("중식"),
        JAPANESE("일식"),
        WESTERN("양식"),
        DESSERT("간식/디저트"),
        ETC("기타");

        private final String displayName;

        CourseCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CourseDifficulty {
        EASY("쉬움"),
        NORMAL("보통"),
        HARD("어려움");

        private final String displayName;

        CourseDifficulty(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CourseStatus {
        DRAFT("임시저장"),
        OPEN("공개"),
        CLOSED("비공개");

        private final String displayName;

        CourseStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
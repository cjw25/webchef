package com.example.fivechef.WebChef.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(length = 500)
    private String videoUrl;

    @Column(nullable = false)
    private Integer price = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SubscriptionPlanType requiredPlanType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Difficulty difficulty;

    @Column(nullable = false, length = 50)
    private String cookTime;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private Integer difficultyOrder = 99;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourseStatus status = CourseStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder asc")
    private List<CourseSession> sessions = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseComment> comments = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.price == null) {
            this.price = 0;
        }

        if (this.status == null) {
            this.status = CourseStatus.PENDING;
        }

        if (this.viewCount < 0) {
            this.viewCount = 0;
        }

        this.difficultyOrder = convertDifficultyOrder(this.difficulty);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.difficultyOrder = convertDifficultyOrder(this.difficulty);
    }

    private int convertDifficultyOrder(Difficulty difficulty) {
        if (difficulty == null) {
            return 99;
        }

        return switch (difficulty) {
            case EASY -> 1;
            case NORMAL -> 2;
            case HARD -> 3;
        };
    }
}
package com.example.fivechef.WebChef.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 레시피 제목
    @Column(nullable = false, length = 100)
    private String title;

    // 한식, 중식, 일식, 양식, 디저트 등
    @Column(length = 50)
    private String category;

    // 대표 재료
    @Column(length = 100)
    private String mainIngredient;

    // 쉬움, 보통, 어려움
    @Column(length = 30)
    private String difficulty;

    // 조리 시간
    private Integer cookingTime;

    // 레시피 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 재료 목록
    @Column(columnDefinition = "TEXT")
    private String ingredients;

    // 조리 순서
    @Column(columnDefinition = "TEXT")
    private String cookingStep;

    // 썸네일 이미지
    private String thumbnailUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
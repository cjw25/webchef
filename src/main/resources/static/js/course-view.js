package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Course;
import com.example.fivechef.WebChef.entity.CourseCategory;
import com.example.fivechef.WebChef.entity.CourseStatus;
import com.example.fivechef.WebChef.entity.Difficulty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CourseResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final String thumbnailUrl;
    private final String videoUrl;
    private final Integer price;
    private final CourseCategory category;
    private final Difficulty difficulty;
    private final CourseStatus status;
    private final String cookTime;

    private final Long instructorId;
    private final String instructorName;
    private final String instructorUsername;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String difficultyLabel;

    // 목록(list) 화면에서 구독 플랜 접근 제어에 쓰는 값들 (상세 화면에서는 사용 안 함, 기본값 유지)
    private String redirectUrl;
    private String accessMessage;
    private boolean canAccess = true;

    public CourseResponse(Course course) {
        this.id = course.getId();
        this.title = course.getTitle();
        this.description = course.getDescription();
        this.thumbnailUrl = course.getThumbnailUrl();
        this.videoUrl = course.getVideoUrl();
        this.price = course.getPrice();
        this.category = course.getCategory();
        this.difficulty = course.getDifficulty();
        this.status = course.getStatus();
        this.cookTime = course.getCookTime();
        this.difficultyLabel = toDifficultyLabel(course.getDifficulty());

        if (course.getInstructor() != null) {
            this.instructorId = course.getInstructor().getId();
            this.instructorName = course.getInstructor().getName();
            this.instructorUsername = course.getInstructor().getUsername();
        } else {
            this.instructorId = null;
            this.instructorName = null;
            this.instructorUsername = null;
        }

        this.createdAt = course.getCreatedAt();
        this.updatedAt = course.getUpdatedAt();
    }

    public CourseResponse(Course course, String redirectUrl, String accessMessage, boolean canAccess) {
        this(course);
        this.redirectUrl = redirectUrl;
        this.accessMessage = accessMessage;
        this.canAccess = canAccess;
    }

    public boolean hasFile() {
        return this.thumbnailUrl != null && !this.thumbnailUrl.isBlank();
    }

    public boolean hasVideo() {
        return this.videoUrl != null && !this.videoUrl.isBlank();
    }

    private static String toDifficultyLabel(Difficulty difficulty) {
        if (difficulty == null) {
            return "";
        }

        return switch (difficulty) {
            case EASY -> "쉬움";
            case NORMAL -> "보통";
            case HARD -> "어려움";
        };
    }
}
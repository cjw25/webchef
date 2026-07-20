package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Course;
import com.example.fivechef.WebChef.entity.CourseCategory;
import com.example.fivechef.WebChef.entity.CourseStatus;
import com.example.fivechef.WebChef.entity.Difficulty;
import com.example.fivechef.WebChef.entity.SubscriptionPlanType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CourseResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final String thumbnailUrl;
    private final Integer price;

    private final SubscriptionPlanType requiredPlanType;
    private final String requiredPlanName;

    private final CourseCategory category;
    private final Difficulty difficulty;
    private final CourseStatus status;
    private final String statusLabel;

    private final String cookTime;
    private final String videoUrl;
    private final String youtubeEmbedUrl;

    private final Long instructorId;
    private final String instructorName;
    private final String instructorUsername;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String difficultyLabel;

    private final String listTargetUrl;
    private final String listAccessMessage;
    private final boolean listAccessible;

    public CourseResponse(Course course) {
        this(
                course,
                "/course/view/" + course.getId(),
                "강의 상세 보기",
                true
        );
    }

    public CourseResponse(
            Course course,
            String listTargetUrl,
            String listAccessMessage,
            boolean listAccessible
    ) {
        this.id = course.getId();
        this.title = course.getTitle();
        this.description = course.getDescription();
        this.thumbnailUrl = course.getThumbnailUrl();
        this.price = course.getPrice();

        this.requiredPlanType = course.getRequiredPlanType();
        this.requiredPlanName = convertPlanName(course.getRequiredPlanType());

        this.category = course.getCategory();
        this.difficulty = course.getDifficulty();
        this.status = course.getStatus();
        this.statusLabel = convertStatusLabel(course.getStatus());

        this.cookTime = course.getCookTime();
        this.videoUrl = course.getVideoUrl();
        this.youtubeEmbedUrl = convertYoutubeEmbedUrl(course.getVideoUrl());

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

        this.listTargetUrl = listTargetUrl;
        this.listAccessMessage = listAccessMessage;
        this.listAccessible = listAccessible;
    }

    private String convertPlanName(SubscriptionPlanType planType) {
        if (planType == null) {
            return "무료";
        }

        if (planType == SubscriptionPlanType.BASIC) {
            return "BASIC";
        }

        if (planType == SubscriptionPlanType.PREMIUM) {
            return "PREMIUM";
        }

        return "-";
    }

    private String convertStatusLabel(CourseStatus status) {
        if (status == null) {
            return "-";
        }

        return switch (status) {
            case PENDING -> "승인 대기";
            case OPEN -> "승인 완료";
            case REJECTED -> "반려";
            case UPDATE_PENDING -> "수정 승인 대기";
            case CLOSED -> "비공개";
        };
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

    private String convertYoutubeEmbedUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        if (url.contains("youtube.com/embed/")) {
            if (url.contains("enablejsapi=1")) {
                return url;
            }
            return url + (url.contains("?") ? "&" : "?") + "enablejsapi=1";
        }

        String videoId = null;

        if (url.contains("youtu.be/")) {
            videoId = url.substring(url.indexOf("youtu.be/") + "youtu.be/".length());
        } else if (url.contains("watch?v=")) {
            videoId = url.substring(url.indexOf("watch?v=") + "watch?v=".length());
        } else if (url.contains("youtube.com/shorts/")) {
            videoId = url.substring(url.indexOf("youtube.com/shorts/") + "youtube.com/shorts/".length());
        }

        if (videoId == null || videoId.isBlank()) {
            return url;
        }

        if (videoId.contains("?")) {
            videoId = videoId.substring(0, videoId.indexOf("?"));
        }

        if (videoId.contains("&")) {
            videoId = videoId.substring(0, videoId.indexOf("&"));
        }

        if (videoId.contains("/")) {
            videoId = videoId.substring(0, videoId.indexOf("/"));
        }

        return "https://www.youtube.com/embed/" + videoId + "?enablejsapi=1";
    }

    public boolean isFree() {
        return this.requiredPlanType == null;
    }

    public boolean isBasicRequired() {
        return this.requiredPlanType == SubscriptionPlanType.BASIC;
    }

    public boolean isPremiumRequired() {
        return this.requiredPlanType == SubscriptionPlanType.PREMIUM;
    }

    public boolean hasFile() {
        return this.thumbnailUrl != null && !this.thumbnailUrl.isBlank();
    }

    public boolean hasVideo() {
        return this.videoUrl != null && !this.videoUrl.isBlank();
    }
}
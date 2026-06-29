package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Course;
import com.example.fivechef.WebChef.entity.Course.CourseCategory;
import com.example.fivechef.WebChef.entity.Course.CourseDifficulty;
import com.example.fivechef.WebChef.entity.Course.CourseStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CourseDetailResponse {

    private Long id;

    private String title;

    private String summary;

    private String description;

    private String thumbnailUrl;

    private int price;

    private boolean free;

    private CourseCategory category;

    private CourseDifficulty difficulty;

    private CourseStatus status;

    private String categoryName;

    private String difficultyName;

    private String statusName;

    private Long instructorId;

    private String instructorName;

    private int viewCount;

    private int studentCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static CourseDetailResponse from(Course course) {
        return CourseDetailResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .summary(course.getSummary())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice())
                .free(course.isFree())
                .category(course.getCategory())
                .difficulty(course.getDifficulty())
                .status(course.getStatus())
                .categoryName(course.getCategory().getDisplayName())
                .difficultyName(course.getDifficulty().getDisplayName())
                .statusName(course.getStatus().getDisplayName())
                .instructorId(course.getInstructorId())
                .instructorName(course.getInstructorName())
                .viewCount(course.getViewCount())
                .studentCount(course.getStudentCount())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
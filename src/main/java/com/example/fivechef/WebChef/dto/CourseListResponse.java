package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Course;
import com.example.fivechef.WebChef.entity.Course.CourseCategory;
import com.example.fivechef.WebChef.entity.Course.CourseDifficulty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseListResponse {

    private Long id;

    private String title;

    private String summary;

    private String thumbnailUrl;

    private int price;

    private boolean free;

    private CourseCategory category;

    private CourseDifficulty difficulty;

    private String categoryName;

    private String difficultyName;

    private String instructorName;

    private int viewCount;

    private int studentCount;

    public static CourseListResponse from(Course course) {
        return CourseListResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .summary(course.getSummary())
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice())
                .free(course.isFree())
                .category(course.getCategory())
                .difficulty(course.getDifficulty())
                .categoryName(course.getCategory().getDisplayName())
                .difficultyName(course.getDifficulty().getDisplayName())
                .instructorName(course.getInstructorName())
                .viewCount(course.getViewCount())
                .studentCount(course.getStudentCount())
                .build();
    }
}
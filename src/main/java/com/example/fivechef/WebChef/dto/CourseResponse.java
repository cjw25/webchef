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
    private final Integer price;
    private final CourseCategory category;
    private final Difficulty difficulty;
    private final CourseStatus status;

    private final Long instructorId;
    private final String instructorName;
    private final String instructorUsername;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CourseResponse(Course course) {
        this.id = course.getId();
        this.title = course.getTitle();
        this.description = course.getDescription();
        this.thumbnailUrl = course.getThumbnailUrl();
        this.price = course.getPrice();
        this.category = course.getCategory();
        this.difficulty = course.getDifficulty();
        this.status = course.getStatus();

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
}
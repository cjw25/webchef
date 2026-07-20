package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Course;
import lombok.Getter;

@Getter
public class FridgeCourseResponse {

    private final Long id;

    private final String title;

    private final String category;

    private final String difficulty;

    private final String description;

    // 추가
    private final String thumbnailUrl;

    // 추가
    private final Integer price;

    public FridgeCourseResponse(Course course) {

        this.id = course.getId();

        this.title = course.getTitle();

        this.category = course.getCategory() == null
                ? null
                : course.getCategory().toString();

        this.difficulty = course.getDifficulty() == null
                ? null
                : course.getDifficulty().toString();

        this.description = course.getDescription();

        // 추가
        this.thumbnailUrl = course.getThumbnailUrl();

        // 추가
        this.price = course.getPrice();
    }
}
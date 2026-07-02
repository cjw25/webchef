package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.CourseCategory;
import com.example.fivechef.WebChef.entity.CourseStatus;
import com.example.fivechef.WebChef.entity.Difficulty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseUpdateRequest {

    private String title;

    private String description;

    private String thumbnailUrl;

    private Integer price;

    private CourseCategory category;

    private Difficulty difficulty;

    private CourseStatus status;
}
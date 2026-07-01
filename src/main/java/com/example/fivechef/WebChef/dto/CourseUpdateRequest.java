package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Course.CourseCategory;
import com.example.fivechef.WebChef.entity.Course.CourseDifficulty;
import com.example.fivechef.WebChef.entity.Course.CourseStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseUpdateRequest {

    private String title;

    private String summary;

    private String description;

    private String thumbnailUrl;

    private int price;

    private boolean free;

    private CourseCategory category;

    private CourseDifficulty difficulty;

    private CourseStatus status;
}
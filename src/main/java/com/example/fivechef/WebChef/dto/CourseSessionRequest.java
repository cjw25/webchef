package com.example.fivechef.WebChef.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseSessionRequest {

    private String title;

    private String videoUrl;

    private int sortOrder;
}
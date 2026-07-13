package com.example.fivechef.WebChef.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseCommentCreateRequest {

    private Long courseId;

    private String content;
}

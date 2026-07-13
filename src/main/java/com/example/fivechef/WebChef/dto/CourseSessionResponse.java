package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.CourseSession;
import lombok.Getter;

@Getter
public class CourseSessionResponse {

    private final Long id;
    private final String title;
    private final String videoUrl;
    private final int sortOrder;
    private final boolean hasQuiz;

    public CourseSessionResponse(CourseSession session, boolean hasQuiz) {
        this.id = session.getId();
        this.title = session.getTitle();
        this.videoUrl = session.getVideoUrl();
        this.sortOrder = session.getSortOrder();
        this.hasQuiz = hasQuiz;
    }
}

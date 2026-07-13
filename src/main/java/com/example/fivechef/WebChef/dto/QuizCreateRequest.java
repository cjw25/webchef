package com.example.fivechef.WebChef.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizCreateRequest {

    private Long sessionId;

    private String title;

    private List<QuestionRequest> questions;

    @Getter
    @Setter
    public static class QuestionRequest {
        private String content;
        private List<ChoiceRequest> choices;
    }

    @Getter
    @Setter
    public static class ChoiceRequest {
        private String content;
        private boolean correct;
    }
}
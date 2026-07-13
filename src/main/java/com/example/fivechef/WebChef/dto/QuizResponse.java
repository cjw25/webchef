package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Quiz;
import com.example.fivechef.WebChef.entity.QuizChoice;
import com.example.fivechef.WebChef.entity.QuizQuestion;
import lombok.Getter;

import java.util.List;

@Getter
public class QuizResponse {

    private final Long id;
    private final String title;
    private final List<QuestionResponse> questions;

    public QuizResponse(Quiz quiz) {
        this.id = quiz.getId();
        this.title = quiz.getTitle();
        this.questions = quiz.getQuestions().stream()
                .map(QuestionResponse::new)
                .toList();
    }

    @Getter
    public static class QuestionResponse {
        private final Long id;
        private final String content;
        private final List<ChoiceResponse> choices;

        public QuestionResponse(QuizQuestion question) {
            this.id = question.getId();
            this.content = question.getContent();
            this.choices = question.getChoices().stream()
                    .map(ChoiceResponse::new)
                    .toList();
        }
    }

    @Getter
    public static class ChoiceResponse {
        private final Long id;
        private final String content;
        // correct 여부는 학생용 응답에는 절대 안 넣어요 (정답 유출 방지)

        public ChoiceResponse(QuizChoice choice) {
            this.id = choice.getId();
            this.content = choice.getContent();
        }
    }
}
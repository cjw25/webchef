package com.example.fivechef.WebChef.dto;

import lombok.Getter;

@Getter
public class QuizResultResponse {

    private static final double PASS_RATE = 0.6;

    private final int score;
    private final int totalCount;
    private final boolean passed;

    public QuizResultResponse(int score, int totalCount) {
        this.score = score;
        this.totalCount = totalCount;
        this.passed = totalCount > 0 && ((double) score / totalCount) >= PASS_RATE;
    }
}
package com.example.fivechef.WebChef.dto;

import lombok.Getter;

@Getter
public class ChatResponse {

    private final boolean success;

    // 기존 JS가 message를 쓰는 경우
    private final String message;

    // 기존 JS가 reply를 쓰는 경우
    private final String reply;

    // 혹시 answer를 쓰는 경우까지 대비
    private final String answer;

    public ChatResponse(boolean success, String text) {
        this.success = success;
        this.message = text;
        this.reply = text;
        this.answer = text;
    }

    public static ChatResponse ok(String text) {
        return new ChatResponse(true, text);
    }

    public static ChatResponse fail(String text) {
        return new ChatResponse(false, text);
    }
}
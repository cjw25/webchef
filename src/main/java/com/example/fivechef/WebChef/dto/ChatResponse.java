package com.example.fivechef.WebChef.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatResponse {

    private boolean success;

    private String answer;

    public ChatResponse() {
    }

    public ChatResponse(boolean success, String answer) {
        this.success = success;
        this.answer = answer;
    }
}
package com.example.fivechef.WebChef.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatResponse {

    private boolean success;
    private String reply;

    public static ChatResponse ok(String reply) {
        return new ChatResponse(true, reply);
    }

    public static ChatResponse fail(String reply) {
        return new ChatResponse(false, reply);
    }
}
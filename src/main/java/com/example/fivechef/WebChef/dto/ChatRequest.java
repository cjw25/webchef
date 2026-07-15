package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.ChatType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {

    private String message;

    private ChatType chatType;
}
package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.ChatRequest;
import com.example.fivechef.WebChef.dto.ChatResponse;
import com.example.fivechef.WebChef.service.OpenAiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    private final OpenAiChatService openAiChatService;

    @PostMapping("/message")
    public ChatResponse message(@RequestBody ChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ChatResponse.fail("질문을 입력해주세요.");
        }

        String reply = openAiChatService.ask(request.getMessage());

        return ChatResponse.ok(reply);
    }
}
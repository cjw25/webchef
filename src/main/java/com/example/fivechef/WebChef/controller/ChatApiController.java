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
        if (request == null || isBlank(request.getMessage())) {
            return ChatResponse.fail("질문을 입력해주세요.");
        }

        try {
            String reply = openAiChatService.ask(request.getMessage());
            return ChatResponse.ok(reply);
        } catch (Exception e) {
            return ChatResponse.fail("AI 챗봇 처리 중 오류가 발생했습니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
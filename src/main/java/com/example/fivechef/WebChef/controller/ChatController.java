package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.ChatRequest;
import com.example.fivechef.WebChef.dto.ChatResponse;
import com.example.fivechef.WebChef.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/chatbot")
    public String chatbotPage() {
        return "chatbot/index";
    }

    @ResponseBody
    @PostMapping("/api/chatbot/message")
    public ChatResponse sendMessage(
            @RequestBody ChatRequest request,
            Authentication authentication
    ) {
        String username = "anonymous";

        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();
        }

        return chatService.ask(request, username);
    }
}
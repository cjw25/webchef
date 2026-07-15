package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.ChatRequest;
import com.example.fivechef.WebChef.dto.ChatResponse;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.service.ChatUsagePolicyService;
import com.example.fivechef.WebChef.service.OpenAiChatService;
import com.example.fivechef.WebChef.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    private final OpenAiChatService openAiChatService;

    private final UserService userService;

    private final ChatUsagePolicyService chatUsagePolicyService;

    @PostMapping("/message")
    public ChatResponse message(
            @RequestBody ChatRequest request,
            Principal principal
    ) {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ChatResponse.fail("메시지를 입력해주세요.");
        }

        User user = null;

        if (principal != null) {
            user = userService.getLoginUserEntity(principal.getName());
        }

        try {
            chatUsagePolicyService.validateChatAccess(user, request.getChatType());

            String reply = openAiChatService.ask(request.getMessage());

            chatUsagePolicyService.increaseUsageIfNeeded(user);

            return ChatResponse.ok(reply);

        } catch (IllegalArgumentException e) {
            return ChatResponse.fail(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            return ChatResponse.fail("챗봇 처리 중 오류가 발생했습니다.");
        }
    }
}
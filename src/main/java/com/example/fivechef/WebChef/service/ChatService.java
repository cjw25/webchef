package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.ChatRequest;
import com.example.fivechef.WebChef.dto.ChatResponse;
import com.example.fivechef.WebChef.entity.ChatMessage;
import com.example.fivechef.WebChef.repository.ChatRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${chatbot.api.url}")
    private String chatbotApiUrl;

    @Value("${chatbot.api.key:}")
    private String chatbotApiKey;

    @Value("${chatbot.api.model:}")
    private String chatbotApiModel;

    public ChatResponse ask(ChatRequest request, String username) {
        String question = request.getMessage();

        if (question == null || question.trim().isEmpty()) {
            return new ChatResponse(false, "질문을 입력해주세요.");
        }

        String answer = callChatbotApi(question);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUsername(username);
        chatMessage.setQuestion(question);
        chatMessage.setAnswer(answer);

        chatRepository.save(chatMessage);

        return new ChatResponse(true, answer);
    }

    private String callChatbotApi(String question) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (chatbotApiKey != null && !chatbotApiKey.isBlank()) {
                headers.setBearerAuth(chatbotApiKey);
            }

            Map<String, Object> body = new HashMap<>();

            if (chatbotApiModel != null && !chatbotApiModel.isBlank()) {
                body.put("model", chatbotApiModel);
            }

            body.put("messages", List.of(
                    Map.of(
                            "role", "system",
                            "content", "너는 WebChef의 자취 생활 학습 도우미 챗봇이다. 요리, 청소, 식비 절약, 자취 생활 팁, 강의 추천을 한국어로 친절하게 답변한다."
                    ),
                    Map.of(
                            "role", "user",
                            "content", question
                    )
            ));

            HttpEntity<Map<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    chatbotApiUrl,
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("챗봇 API 호출에 실패했습니다.");
            }

            return extractAnswer(response.getBody());

        } catch (Exception e) {
            return "챗봇 API 연동 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    private String extractAnswer(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        if (root.has("answer")) {
            return root.get("answer").asText();
        }

        if (root.has("message")) {
            return root.get("message").asText();
        }

        JsonNode choices = root.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            JsonNode content = choices.get(0).path("message").path("content");

            if (!content.isMissingNode() && !content.asText().isBlank()) {
                return content.asText();
            }
        }

        JsonNode outputText = root.path("output_text");
        if (!outputText.isMissingNode() && !outputText.asText().isBlank()) {
            return outputText.asText();
        }

        throw new RuntimeException("챗봇 응답에서 답변을 찾을 수 없습니다.");
    }
}
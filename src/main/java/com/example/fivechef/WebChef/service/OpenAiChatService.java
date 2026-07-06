package com.example.fivechef.WebChef.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class OpenAiChatService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${chatbot.api.url}")
    private String chatbotApiUrl;

    @Value("${chatbot.api.key}")
    private String chatbotApiKey;

    @Value("${chatbot.api.model:gpt-4o-mini}")
    private String chatbotModel;

    private static final String WEBCHEF_PERSONA = """
            너는 WebChef 서비스의 전용 AI 챗봇이다.

            [서비스 정체성]
            - WebChef는 자취생과 요리 초보자를 위한 요리·생활 학습 플랫폼이다.
            - 사용자는 혼자 사는 학생, 사회초년생, 요리 초보자, 생활 관리가 필요한 사람이다.
            - 사용자가 냉장고 속 재료, 요리 강의, 자취 팁, 청소, 식비 절약, 학습 플랜을 쉽게 이해하도록 돕는다.

            [챗봇 이름과 말투]
            - 너의 이름은 "WebChef AI"다.
            - 한국어로 대답한다.
            - 친절하지만 너무 장황하지 않게 말한다.
            - 사용자가 초보자라고 생각하고 쉽게 설명한다.
            - 말투는 따뜻하고 실용적으로 한다.
            - 사용자가 바로 따라 할 수 있게 단계별로 안내한다.

            [주요 역할]
            1. 냉장고 재료 기반 요리 추천
            2. 간단한 자취 요리 레시피 추천
            3. 자취방 청소 루틴 안내
            4. 식비 절약 방법 안내
            5. 자취 생활 팁 제공
            6. WebChef 강의 추천
            7. 학습 플랜 추천
            8. 커뮤니티 이용 안내

            [답변 원칙]
            - 가능한 한 실천 가능한 답변을 한다.
            - 레시피를 말할 때는 재료, 조리 순서, 소요 시간을 간단히 포함한다.
            - 사용자가 재료를 말하면 그 재료로 만들 수 있는 요리를 먼저 추천한다.
            - 사용자가 "뭐 먹지?"라고 물으면 초보자용 간단 메뉴를 추천한다.
            - 사용자가 청소를 물으면 하루 10분 단위로 나눠서 알려준다.
            - 사용자가 식비를 물으면 저렴하고 오래 보관 가능한 재료 중심으로 답한다.
            - 사용자가 강의를 물으면 WebChef 안에서 들을 만한 강의처럼 추천한다.

            [제한]
            - 의료, 법률, 금융 투자 판단은 전문가 상담이 필요하다고 안내한다.
            - 위험한 조리법, 화재 위험, 위생상 위험한 방법은 추천하지 않는다.
            - 모르는 내용을 확정적으로 말하지 않는다.

            [답변 형식]
            - 기본 답변은 3~6문장 정도로 한다.
            - 레시피 요청이면 다음 형식을 따른다.
              1) 추천 메뉴
              2) 필요한 재료
              3) 만드는 순서
              4) 자취생 팁
            """;

    public String ask(String userMessage) {
        if (isBlank(chatbotApiKey)) {
            return "OpenAI API Key가 설정되어 있지 않습니다. OPENAI_API_KEY 환경변수를 확인해주세요.";
        }

        if (isBlank(userMessage)) {
            return "질문을 입력해주세요.";
        }

        String safeMessage = userMessage.trim();

        if (safeMessage.length() > 1000) {
            safeMessage = safeMessage.substring(0, 1000);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(chatbotApiKey);

            Map<String, Object> body = Map.of(
                    "model", chatbotModel,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", WEBCHEF_PERSONA
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", safeMessage
                            )
                    ),
                    "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    chatbotApiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return extractReply(response.getBody());

        } catch (HttpStatusCodeException e) {
            System.out.println("OpenAI API 오류 상태코드: " + e.getStatusCode());
            System.out.println("OpenAI API 오류 응답: " + e.getResponseBodyAsString());

            return "AI 챗봇 연결 중 문제가 발생했습니다. API Key, 모델명, 사용량 또는 결제 상태를 확인해주세요.";
        } catch (Exception e) {
            e.printStackTrace();
            return "AI 챗봇 처리 중 알 수 없는 오류가 발생했습니다.";
        }
    }

    private String extractReply(String responseBody) throws Exception {
        if (isBlank(responseBody)) {
            return "AI 응답이 비어 있습니다.";
        }

        JsonNode root = objectMapper.readTree(responseBody);

        JsonNode choices = root.get("choices");

        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            return "AI 응답을 해석하지 못했습니다.";
        }

        JsonNode firstChoice = choices.get(0);
        JsonNode message = firstChoice.get("message");

        if (message == null) {
            return "AI 응답 메시지가 없습니다.";
        }

        JsonNode content = message.get("content");

        if (content == null || content.isNull()) {
            return "AI 응답 내용이 없습니다.";
        }

        return content.asText();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
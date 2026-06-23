//package com.example.fivechef.WebChef.service;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class ChatService {
//
//    @Value("${api.key}")
//    private String apiKey;
//
//    @Value("${gemini.model}")//키변경하기
//    private String modelName;
//
//    public String askToAI(String question){
//        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=" + apiKey;
//        System.out.println("키 확인: " + apiKey);
//
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        String modifiedPrompt = question;
//
//        Map<String, Object> body = new HashMap<>();
//        Map<String, Object> textPart = new HashMap<>();
//        textPart.put("text", modifiedPrompt);
//
//        Map<String, Object> partContainer = new HashMap<>();
//        partContainer.put("parts", List.of(textPart));
//        body.put("contents", List.of(partContainer));
//
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
//
//        try {
//            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
//            Map responseBody = response.getBody();
//
//            List candidates = (List) responseBody.get("candidates");
//            if (candidates != null && !candidates.isEmpty()){
//                Map firstCandidate = (Map) candidates.get(0);
//
//                Map content = (Map) firstCandidate.get("content");
//
//                List resParts = (List) content.get("parts");
//                Map firstPart = (Map) resParts.get(0);
//                return firstPart.get("test").toString();
//            }
//            return "답변을 생성하지 못했습니다.";
//
//        } catch (Exception e){
//            e.printStackTrace();
//            return "최종 연결 실패 사유: " + e.getMessage();
//        }
//    }
//}

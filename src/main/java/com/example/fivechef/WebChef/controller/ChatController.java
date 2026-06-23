package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.ChatRequestDTO;
import com.example.fivechef.WebChef.dto.ChatResponseDTO;
import com.example.fivechef.WebChef.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

//    @PostMapping("/ask")
//    public ResponseEntity<ChatResponseDTO> ask(@RequestBody ChatRequestDTO request) {
//        //String answer = chatService.askToAI(request.getQuestion());
//        // return ResponseEntity.ok(new ChatResponseDTO(answer))
//  }
}

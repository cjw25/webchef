package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByUsernameOrderByCreatedAtDesc(String username);
}
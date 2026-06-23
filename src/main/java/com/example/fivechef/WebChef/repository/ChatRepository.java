package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.ChatMassage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<ChatMassage, Long> {
}

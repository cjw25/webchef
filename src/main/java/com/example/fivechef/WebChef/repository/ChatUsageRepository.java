package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.ChatUsage;
import com.example.fivechef.WebChef.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ChatUsageRepository extends JpaRepository<ChatUsage, Long> {

    Optional<ChatUsage> findByUserAndUsageDate(User user, LocalDate usageDate);
}
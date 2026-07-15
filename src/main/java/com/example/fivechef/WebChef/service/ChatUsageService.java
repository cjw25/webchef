package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.entity.ChatUsage;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.ChatUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class ChatUsageService {

    private final ChatUsageRepository chatUsageRepository;

    @Transactional(readOnly = true)
    public boolean canUseToday(User user, int dailyLimit) {
        if (user == null) {
            return false;
        }

        ChatUsage usage = chatUsageRepository
                .findByUserAndUsageDate(user, LocalDate.now())
                .orElse(null);

        if (usage == null) {
            return true;
        }

        return usage.getCount() < dailyLimit;
    }

    @Transactional
    public void increaseToday(User user) {
        if (user == null) {
            return;
        }

        ChatUsage usage = chatUsageRepository
                .findByUserAndUsageDate(user, LocalDate.now())
                .orElseGet(() -> {
                    ChatUsage newUsage = new ChatUsage();
                    newUsage.setUser(user);
                    newUsage.setUsageDate(LocalDate.now());
                    newUsage.setCount(0);
                    return newUsage;
                });

        usage.setCount(usage.getCount() + 1);

        chatUsageRepository.save(usage);
    }
}
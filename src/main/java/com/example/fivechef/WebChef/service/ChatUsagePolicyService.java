package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.entity.ChatType;
import com.example.fivechef.WebChef.entity.SubscriptionPlanType;
import com.example.fivechef.WebChef.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ChatUsagePolicyService {

    private static final int BASIC_DAILY_LIMIT = 10;

    private final SubscriptionService subscriptionService;

    private final ChatUsageService chatUsageService;

    /*
     * 무료 USER
     * - 고객센터 챗봇만 가능
     *
     * BASIC USER
     * - 챗봇 가능
     * - 하루 10회 제한
     *
     * PREMIUM USER
     * - 챗봇 무제한
     */
    public void validateChatAccess(User user, ChatType chatType) {
        if (chatType == null) {
            chatType = ChatType.CUSTOMER_SUPPORT;
        }

        SubscriptionPlanType currentPlan = subscriptionService.getCurrentPlan(user);

        // 무료 사용자
        if (currentPlan == null) {
            if (chatType != ChatType.CUSTOMER_SUPPORT) {
                throw new IllegalArgumentException("무료 회원은 고객센터 챗봇만 이용할 수 있습니다.");
            }

            return;
        }

        // BASIC 구독자
        if (currentPlan == SubscriptionPlanType.BASIC) {
            if (!chatUsageService.canUseToday(user, BASIC_DAILY_LIMIT)) {
                throw new IllegalArgumentException("BASIC 구독권의 오늘 챗봇 사용 횟수를 모두 사용했습니다.");
            }

            return;
        }

        // PREMIUM 구독자
        if (currentPlan == SubscriptionPlanType.PREMIUM) {
            return;
        }
    }

    public void increaseUsageIfNeeded(User user) {
        SubscriptionPlanType currentPlan = subscriptionService.getCurrentPlan(user);

        if (currentPlan == SubscriptionPlanType.BASIC) {
            chatUsageService.increaseToday(user);
        }
    }
}
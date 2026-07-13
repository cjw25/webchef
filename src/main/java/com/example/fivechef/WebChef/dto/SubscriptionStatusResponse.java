package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.SubscriptionPlanType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SubscriptionStatusResponse {

    private boolean subscribed;

    private SubscriptionPlanType planType;

    private String planName;

    private LocalDateTime expiredAt;

    private boolean basic;

    private boolean premium;

    public static SubscriptionStatusResponse none() {
        return new SubscriptionStatusResponse(
                false,
                null,
                "미구독",
                null,
                false,
                false
        );
    }

    public static SubscriptionStatusResponse subscribed(
            SubscriptionPlanType planType,
            LocalDateTime expiredAt
    ) {
        return new SubscriptionStatusResponse(
                true,
                planType,
                planType == null ? "미구독" : planType.name(),
                expiredAt,
                planType == SubscriptionPlanType.BASIC,
                planType == SubscriptionPlanType.PREMIUM
        );
    }
}
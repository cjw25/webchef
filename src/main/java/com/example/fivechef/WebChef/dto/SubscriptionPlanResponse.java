package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.SubscriptionPlanType;
import lombok.Getter;

@Getter
public class SubscriptionPlanResponse {

    private final SubscriptionPlanType planType;

    private final String name;

    private final String title;

    private final Integer price;

    private final String description;

    private final String features;

    public SubscriptionPlanResponse(
            SubscriptionPlanType planType,
            String name,
            String title,
            Integer price,
            String description,
            String features
    ) {
        this.planType = planType;
        this.name = name;
        this.title = title;
        this.price = price;
        this.description = description;
        this.features = features;
    }

    public String getPlanName() {
        return name;
    }

    public Integer getAmount() {
        return price;
    }

    public boolean isBasic() {
        return planType == SubscriptionPlanType.BASIC;
    }

    public boolean isPremium() {
        return planType == SubscriptionPlanType.PREMIUM;
    }
}
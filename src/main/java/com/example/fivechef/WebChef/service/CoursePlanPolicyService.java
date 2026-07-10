package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.entity.SubscriptionPlanType;
import org.springframework.stereotype.Service;

@Service
public class CoursePlanPolicyService {

    private static final int PREMIUM_PRICE_THRESHOLD = 200000;

    public SubscriptionPlanType decideRequiredPlan(Integer price) {
        if (price == null || price <= 0) {
            return null;
        }

        if (price <= PREMIUM_PRICE_THRESHOLD) {
            return SubscriptionPlanType.BASIC;
        }

        return SubscriptionPlanType.PREMIUM;
    }

    public boolean canAccess(
            SubscriptionPlanType userPlan,
            SubscriptionPlanType requiredPlan
    ) {
        if (requiredPlan == null) {
            return true;
        }

        if (userPlan == null) {
            return false;
        }

        if (requiredPlan == SubscriptionPlanType.BASIC) {
            return userPlan == SubscriptionPlanType.BASIC
                    || userPlan == SubscriptionPlanType.PREMIUM;
        }

        if (requiredPlan == SubscriptionPlanType.PREMIUM) {
            return userPlan == SubscriptionPlanType.PREMIUM;
        }

        return false;
    }
}
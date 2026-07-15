package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.entity.SubscriptionPlanType;
import org.springframework.stereotype.Service;

@Service
public class CoursePlanPolicyService {

    private static final int PREMIUM_PRICE_THRESHOLD = 200000;

    /*
     * 강의 가격으로 필요한 최소 구독권 결정
     */
    public SubscriptionPlanType decideRequiredPlan(Integer price) {
        if (price == null || price <= 0) {
            return null;
        }

        if (price <= PREMIUM_PRICE_THRESHOLD) {
            return SubscriptionPlanType.BASIC;
        }

        return SubscriptionPlanType.PREMIUM;
    }

    /*
     * 수강 가능 여부
     *
     * BASIC 구독권
     * - BASIC 강의만 가능
     *
     * PREMIUM 구독권
     * - BASIC + PREMIUM 모두 가능
     */
    public boolean canAccess(SubscriptionPlanType userPlan, SubscriptionPlanType requiredPlan) {
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
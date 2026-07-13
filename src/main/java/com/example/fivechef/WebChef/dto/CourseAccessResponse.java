package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.SubscriptionPlanType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourseAccessResponse {

    private boolean accessible;

    private boolean paymentRequired;

    private SubscriptionPlanType requiredPlanType;

    private String requiredPlanName;

    private String message;

    private String buttonText;

    private String redirectUrl;

    public static CourseAccessResponse start(Long courseId) {
        return new CourseAccessResponse(
                true,
                false,
                null,
                "무료",
                "수강 가능합니다.",
                "수강 시작",
                "/course/watch/" + courseId
        );
    }

    public static CourseAccessResponse needSubscription(
            Long courseId,
            SubscriptionPlanType requiredPlanType
    ) {
        String planName = requiredPlanType == null ? "무료" : requiredPlanType.name();

        return new CourseAccessResponse(
                false,
                true,
                requiredPlanType,
                planName,
                planName + " 구독권이 필요한 강의입니다.",
                planName + " 구독하기",
                "/payment/course/" + courseId
        );
    }

    public static CourseAccessResponse needUpgrade(
            Long courseId,
            SubscriptionPlanType requiredPlanType
    ) {
        return new CourseAccessResponse(
                false,
                true,
                requiredPlanType,
                requiredPlanType.name(),
                "현재 구독권으로는 수강할 수 없습니다. PREMIUM 구독권이 필요합니다.",
                "PREMIUM으로 업그레이드",
                "/payment/course/" + courseId
        );
    }
}
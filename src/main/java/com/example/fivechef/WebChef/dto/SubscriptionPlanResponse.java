package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.SubscriptionPlanType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubscriptionPlanResponse {

    private SubscriptionPlanType planType;

    private String name;

    private String description;

    private Integer price;

    private String features;

    public static SubscriptionPlanResponse basic() {
        return new SubscriptionPlanResponse(
                SubscriptionPlanType.BASIC,
                "베이직 구독권",
                "20만원 이하 강의를 수강할 수 있는 기본 구독권입니다.",
                9900,
                "20만원 이하 강의 수강,메타버스 이용,AI 챗봇 하루 제한 사용,커뮤니티 이용"
        );
    }

    public static SubscriptionPlanResponse premium() {
        return new SubscriptionPlanResponse(
                SubscriptionPlanType.PREMIUM,
                "프리미엄 구독권",
                "20만원 초과 프리미엄 강의까지 수강할 수 있는 구독권입니다.",
                19900,
                "전체 강의 수강,프리미엄 강의 수강,프리미엄 레시피 이용,메타버스 이용,AI 챗봇 무제한 사용"
        );
    }
}
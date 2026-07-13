package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.SubscriptionPlanType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CoursePaymentPageResponse {

    private Long courseId;

    private String courseTitle;

    private Integer coursePrice;

    private SubscriptionPlanType requiredPlanType;

    private String requiredPlanName;

    private List<SubscriptionPlanResponse> plans;

    private String guideMessage;
}
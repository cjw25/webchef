package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.SubscriptionPlanType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentReadyRequest {

    private Long courseId;

    private SubscriptionPlanType planType;
}
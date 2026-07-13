package com.example.fivechef.WebChef.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentReadyResponse {

    private boolean success;

    private String message;

    private String clientKey;

    private String orderId;

    private String orderName;

    private Integer amount;

    private String customerName;

    private String customerEmail;

    public static PaymentReadyResponse ok(
            String clientKey,
            String orderId,
            String orderName,
            Integer amount,
            String customerName,
            String customerEmail
    ) {
        return new PaymentReadyResponse(
                true,
                "결제 준비가 완료되었습니다.",
                clientKey,
                orderId,
                orderName,
                amount,
                customerName,
                customerEmail
        );
    }

    public static PaymentReadyResponse fail(String message) {
        return new PaymentReadyResponse(
                false,
                message,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
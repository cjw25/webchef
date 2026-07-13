package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.PaymentReadyRequest;
import com.example.fivechef.WebChef.dto.PaymentReadyResponse;
import com.example.fivechef.WebChef.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentApiController {

    private final PaymentService paymentService;

    @PostMapping("/ready")
    public PaymentReadyResponse readyPayment(
            @RequestBody PaymentReadyRequest request,
            Principal principal
    ) {
        if (principal == null) {
            return PaymentReadyResponse.fail("로그인이 필요합니다.");
        }

        try {
            return paymentService.readyPayment(
                    request,
                    principal.getName()
            );
        } catch (Exception e) {
            return PaymentReadyResponse.fail(e.getMessage());
        }
    }
}
package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
public class PaymentPageController {

    private final PaymentService paymentService;

    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Integer amount,
            Model model
    ) {
        try {
            paymentService.confirmPayment(
                    paymentKey,
                    orderId,
                    amount
            );

            model.addAttribute("message", "결제가 완료되었습니다.");
            return "payment/success";

        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
            return "payment/fail";
        }
    }

    @GetMapping("/payment/fail")
    public String paymentFail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            Model model
    ) {
        model.addAttribute("code", code);
        model.addAttribute("message", message == null ? "결제에 실패했습니다." : message);

        return "payment/fail";
    }
}
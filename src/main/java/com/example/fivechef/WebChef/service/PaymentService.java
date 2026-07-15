package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.PaymentReadyRequest;
import com.example.fivechef.WebChef.dto.PaymentReadyResponse;
import com.example.fivechef.WebChef.dto.SubscriptionPlanResponse;
import com.example.fivechef.WebChef.entity.*;
import com.example.fivechef.WebChef.repository.CourseRepository;
import com.example.fivechef.WebChef.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final CourseRepository courseRepository;

    private final UserService userService;

    private final SubscriptionService subscriptionService;

    private final RestTemplate restTemplate;

    @Value("${toss.client-key}")
    private String tossClientKey;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    @Value("${toss.confirm-url}")
    private String tossConfirmUrl;

    @Transactional
    public PaymentReadyResponse readyPayment(
            PaymentReadyRequest request,
            String username
    ) {
        validateReadyRequest(request);

        User user = userService.getLoginUserEntity(username);

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        SubscriptionPlanType requiredPlan = course.getRequiredPlanType();

        if (requiredPlan == null) {
            throw new IllegalArgumentException("무료 강의는 결제가 필요하지 않습니다.");
        }

        /*
         * 여기서 BASIC / PREMIUM 선택을 막으면 안 됨.
         * 유저는 어떤 유료 강의를 눌러도 BASIC / PREMIUM 둘 중 하나를 선택할 수 있어야 함.
         */
        SubscriptionPlanResponse plan = subscriptionService.getPlan(request.getPlanType());

        String orderId = "WEBCHEF-" + UUID.randomUUID().toString().replace("-", "");
        String orderName = plan.getName() + " - " + course.getTitle();

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setProductType(PaymentProductType.SUBSCRIPTION);
        payment.setPlanType(request.getPlanType());
        payment.setCourseId(course.getId());
        payment.setInstructorId(course.getInstructor() == null ? null : course.getInstructor().getId());
        payment.setOrderId(orderId);
        payment.setOrderName(orderName);
        payment.setAmount(plan.getPrice());
        payment.setStatus(PaymentStatus.READY);

        paymentRepository.save(payment);

        return PaymentReadyResponse.ok(
                tossClientKey,
                orderId,
                orderName,
                plan.getPrice(),
                user.getName(),
                user.getEmail()
        );
    }

    @Transactional
    public void confirmPayment(
            String paymentKey,
            String orderId,
            Integer amount
    ) {
        if (paymentKey == null || paymentKey.trim().isEmpty()) {
            throw new IllegalArgumentException("paymentKey가 없습니다.");
        }

        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("orderId가 없습니다.");
        }

        if (amount == null) {
            throw new IllegalArgumentException("결제 금액이 없습니다.");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 요청 정보를 찾을 수 없습니다."));

        if (payment.getStatus() == PaymentStatus.DONE) {
            return;
        }

        if (!payment.getAmount().equals(amount)) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        callTossConfirmApi(paymentKey, orderId, amount);

        payment.setPaymentKey(paymentKey);
        payment.setStatus(PaymentStatus.DONE);
        payment.setApprovedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        subscriptionService.activateSubscription(
                payment.getUser(),
                payment.getPlanType()
        );
    }

    private void callTossConfirmApi(
            String paymentKey,
            String orderId,
            Integer amount
    ) {
        HttpHeaders headers = createTossHeaders();

        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        restTemplate.exchange(
                tossConfirmUrl,
                HttpMethod.POST,
                request,
                String.class
        );
    }

    private HttpHeaders createTossHeaders() {
        if (tossSecretKey == null || tossSecretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("토스 시크릿 키가 설정되어 있지 않습니다.");
        }

        String auth = tossSecretKey + ":";
        String encodedAuth = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);

        return headers;
    }

    private void validateReadyRequest(PaymentReadyRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("결제 요청 정보가 없습니다.");
        }

        if (request.getCourseId() == null) {
            throw new IllegalArgumentException("강의 ID가 없습니다.");
        }

        if (request.getPlanType() == null) {
            throw new IllegalArgumentException("구독권을 선택해주세요.");
        }
    }
}
package com.example.fivechef.WebChef.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 결제한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 구독 결제
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentProductType productType;

    // BASIC / PREMIUM
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionPlanType planType;

    // 어떤 강의 때문에 구독 결제를 했는지
    private Long courseId;

    // 강사 정산용
    private Long instructorId;

    @Column(nullable = false, length = 200)
    private String orderName;

    @Column(nullable = false, unique = true, length = 120)
    private String orderId;

    @Column(length = 200)
    private String paymentKey;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.READY;

    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

    private LocalDateTime canceledAt;

    @Column(length = 255)
    private String cancelReason;

    @PrePersist
    public void prePersist() {
        this.requestedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = PaymentStatus.READY;
        }
    }
}
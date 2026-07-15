package com.example.fivechef.WebChef.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_subscriptions")
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 구독한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // BASIC / PREMIUM
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionPlanType planType;

    // 만료일까지 이용 가능 여부
    @Column(nullable = false)
    private Boolean active = true;

    // 해지 여부
    @Column(nullable = false)
    private Boolean cancelled = false;

    private LocalDateTime startedAt;

    private LocalDateTime expiredAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.active == null) {
            this.active = true;
        }

        if (this.cancelled == null) {
            this.cancelled = false;
        }

        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
    }
}
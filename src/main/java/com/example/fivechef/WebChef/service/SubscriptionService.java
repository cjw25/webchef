package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.SubscriptionPlanResponse;
import com.example.fivechef.WebChef.dto.SubscriptionStatusResponse;
import com.example.fivechef.WebChef.entity.SubscriptionPlanType;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.entity.UserSubscription;
import com.example.fivechef.WebChef.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;

    private final UserService userService;

    public List<SubscriptionPlanResponse> getPlansForCourse(SubscriptionPlanType requiredPlanType) {
        if (requiredPlanType == SubscriptionPlanType.PREMIUM) {
            return List.of(SubscriptionPlanResponse.premium());
        }

        if (requiredPlanType == SubscriptionPlanType.BASIC) {
            return List.of(
                    SubscriptionPlanResponse.basic(),
                    SubscriptionPlanResponse.premium()
            );
        }

        return List.of();
    }

    public SubscriptionPlanResponse getPlan(SubscriptionPlanType planType) {
        if (planType == SubscriptionPlanType.BASIC) {
            return SubscriptionPlanResponse.basic();
        }

        if (planType == SubscriptionPlanType.PREMIUM) {
            return SubscriptionPlanResponse.premium();
        }

        throw new IllegalArgumentException("존재하지 않는 구독권입니다.");
    }

    @Transactional(readOnly = true)
    public SubscriptionStatusResponse getMySubscriptionStatus(String username) {
        if (username == null) {
            return SubscriptionStatusResponse.none();
        }

        User user = userService.getLoginUserEntity(username);

        return userSubscriptionRepository
                .findTopByUserAndActiveTrueAndExpiredAtAfterOrderByIdDesc(
                        user,
                        LocalDateTime.now()
                )
                .map(subscription -> SubscriptionStatusResponse.subscribed(
                        subscription.getPlanType(),
                        subscription.getExpiredAt()
                ))
                .orElse(SubscriptionStatusResponse.none());
    }

    @Transactional(readOnly = true)
    public SubscriptionPlanType getCurrentPlan(User user) {
        return userSubscriptionRepository
                .findTopByUserAndActiveTrueAndExpiredAtAfterOrderByIdDesc(
                        user,
                        LocalDateTime.now()
                )
                .map(UserSubscription::getPlanType)
                .orElse(null);
    }

    @Transactional
    public void activateSubscription(User user, SubscriptionPlanType planType) {
        userSubscriptionRepository.findTopByUserAndActiveTrueOrderByIdDesc(user)
                .ifPresent(subscription -> {
                    subscription.setActive(false);
                    userSubscriptionRepository.save(subscription);
                });

        UserSubscription subscription = new UserSubscription();
        subscription.setUser(user);
        subscription.setPlanType(planType);
        subscription.setActive(true);
        subscription.setStartedAt(LocalDateTime.now());
        subscription.setExpiredAt(LocalDateTime.now().plusMonths(1));

        userSubscriptionRepository.save(subscription);
    }
}
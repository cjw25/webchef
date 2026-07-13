package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.SubscriptionStatusResponse;
import com.example.fivechef.WebChef.entity.SubscriptionPlanType;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.entity.UserSubscription;
import com.example.fivechef.WebChef.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class SubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;

    private final UserService userService;

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
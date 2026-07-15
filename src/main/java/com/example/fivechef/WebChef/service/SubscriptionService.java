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

    /*
     * 구독권 상품 1개 조회
     */
    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getPlan(SubscriptionPlanType planType) {
        if (planType == null) {
            throw new IllegalArgumentException("구독권 종류가 없습니다.");
        }

        if (planType == SubscriptionPlanType.BASIC) {
            return new SubscriptionPlanResponse(
                    SubscriptionPlanType.BASIC,
                    "BASIC",
                    "베이직 구독권",
                    9900,
                    "BASIC 강의를 30일 동안 수강할 수 있습니다. 챗봇은 하루 사용 횟수가 제한됩니다.",
                    "BASIC 강의 수강 가능,30일 이용 가능,챗봇 하루 사용 횟수 제한,자취 요리 학습 가능"
            );
        }

        if (planType == SubscriptionPlanType.PREMIUM) {
            return new SubscriptionPlanResponse(
                    SubscriptionPlanType.PREMIUM,
                    "PREMIUM",
                    "프리미엄 구독권",
                    19900,
                    "BASIC 강의와 PREMIUM 강의를 모두 30일 동안 수강할 수 있습니다. 챗봇 사용 횟수 제한이 없습니다.",
                    "BASIC 강의 수강 가능,PREMIUM 강의 수강 가능,30일 이용 가능,챗봇 무제한 사용"
            );
        }

        throw new IllegalArgumentException("지원하지 않는 구독권입니다.");
    }

    /*
     * 유료 강의를 누르면 항상 BASIC / PREMIUM 둘 다 보여줌
     */
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getPlansForCourse(SubscriptionPlanType requiredPlan) {
        if (requiredPlan == null) {
            return List.of();
        }

        return List.of(
                getPlan(SubscriptionPlanType.BASIC),
                getPlan(SubscriptionPlanType.PREMIUM)
        );
    }

    /*
     * 현재 유저의 구독권 등급 확인
     */
    @Transactional(readOnly = true)
    public SubscriptionPlanType getCurrentPlan(User user) {
        if (user == null) {
            return null;
        }

        return userSubscriptionRepository
                .findTopByUserAndActiveTrueAndExpiredAtAfterOrderByIdDesc(
                        user,
                        LocalDateTime.now()
                )
                .map(UserSubscription::getPlanType)
                .orElse(null);
    }

    /*
     * 마이페이지 > 내 구독권
     */
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
                        subscription.getExpiredAt(),
                        Boolean.TRUE.equals(subscription.getCancelled()),
                        subscription.getCancelledAt()
                ))
                .orElse(SubscriptionStatusResponse.none());
    }

    /*
     * 결제 성공 후 구독권 생성
     */
    @Transactional
    public void activateSubscription(User user, SubscriptionPlanType planType) {
        if (user == null) {
            throw new IllegalArgumentException("사용자 정보가 없습니다.");
        }

        if (planType == null) {
            throw new IllegalArgumentException("구독권 종류가 없습니다.");
        }

        userSubscriptionRepository.findTopByUserAndActiveTrueOrderByIdDesc(user)
                .ifPresent(oldSubscription -> {
                    oldSubscription.setActive(false);
                    userSubscriptionRepository.save(oldSubscription);
                });

        LocalDateTime now = LocalDateTime.now();

        UserSubscription subscription = new UserSubscription();
        subscription.setUser(user);
        subscription.setPlanType(planType);
        subscription.setActive(true);
        subscription.setCancelled(false);
        subscription.setStartedAt(now);
        subscription.setExpiredAt(now.plusDays(30));

        userSubscriptionRepository.save(subscription);
    }

    /*
     * 구독권 해지
     * 해지해도 만료일까지는 이용 가능
     */
    @Transactional
    public void cancelMySubscription(String username) {
        User user = userService.getLoginUserEntity(username);

        UserSubscription subscription = userSubscriptionRepository
                .findTopByUserAndActiveTrueAndExpiredAtAfterOrderByIdDesc(
                        user,
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new IllegalArgumentException("해지할 구독권이 없습니다."));

        if (Boolean.TRUE.equals(subscription.getCancelled())) {
            throw new IllegalArgumentException("이미 해지된 구독권입니다.");
        }

        subscription.setCancelled(true);
        subscription.setCancelledAt(LocalDateTime.now());

        userSubscriptionRepository.save(subscription);
    }
}
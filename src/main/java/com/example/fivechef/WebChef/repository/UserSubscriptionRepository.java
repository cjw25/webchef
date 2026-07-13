package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    Optional<UserSubscription> findTopByUserAndActiveTrueOrderByIdDesc(User user);

    Optional<UserSubscription> findTopByUserAndActiveTrueAndExpiredAtAfterOrderByIdDesc(
            User user,
            LocalDateTime now
    );
}
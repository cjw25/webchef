package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.CourseAccessResponse;
import com.example.fivechef.WebChef.entity.Course;
import com.example.fivechef.WebChef.entity.SubscriptionPlanType;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CourseAccessService {

    private final CourseRepository courseRepository;

    private final UserService userService;

    private final SubscriptionService subscriptionService;

    private final CoursePlanPolicyService coursePlanPolicyService;

    @Transactional(readOnly = true)
    public CourseAccessResponse getCourseAccess(Long courseId, String username) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        SubscriptionPlanType requiredPlan = course.getRequiredPlanType();

        if (requiredPlan == null) {
            return CourseAccessResponse.start(courseId);
        }

        if (username == null) {
            return CourseAccessResponse.needSubscription(courseId, requiredPlan);
        }

        User user = userService.getLoginUserEntity(username);

        SubscriptionPlanType userPlan = subscriptionService.getCurrentPlan(user);

        boolean canAccess = coursePlanPolicyService.canAccess(
                userPlan,
                requiredPlan
        );

        if (canAccess) {
            return CourseAccessResponse.start(courseId);
        }

        if (userPlan == SubscriptionPlanType.BASIC
                && requiredPlan == SubscriptionPlanType.PREMIUM) {
            return CourseAccessResponse.needUpgrade(courseId, requiredPlan);
        }

        return CourseAccessResponse.needSubscription(courseId, requiredPlan);
    }
}
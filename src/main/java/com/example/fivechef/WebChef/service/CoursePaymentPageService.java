package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.CoursePaymentPageResponse;
import com.example.fivechef.WebChef.entity.Course;
import com.example.fivechef.WebChef.entity.SubscriptionPlanType;
import com.example.fivechef.WebChef.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CoursePaymentPageService {

    private final CourseRepository courseRepository;

    private final SubscriptionService subscriptionService;

    @Transactional(readOnly = true)
    public CoursePaymentPageResponse getCoursePaymentPage(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        SubscriptionPlanType requiredPlan = course.getRequiredPlanType();

        if (requiredPlan == null) {
            throw new IllegalArgumentException("무료 강의는 결제가 필요하지 않습니다.");
        }

        String guideMessage;

        if (requiredPlan == SubscriptionPlanType.BASIC) {
            guideMessage = "이 강의는 20만원 이하 강의로 BASIC 이상 구독권이 필요합니다. PREMIUM을 선택하면 모든 강의까지 이용할 수 있습니다.";
        } else {
            guideMessage = "이 강의는 20만원 초과 프리미엄 강의로 PREMIUM 구독권이 필요합니다.";
        }

        return new CoursePaymentPageResponse(
                course.getId(),
                course.getTitle(),
                course.getPrice(),
                requiredPlan,
                requiredPlan.name(),
                subscriptionService.getPlansForCourse(requiredPlan),
                guideMessage
        );
    }
}
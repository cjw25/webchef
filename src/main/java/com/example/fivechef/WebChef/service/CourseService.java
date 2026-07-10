package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.CourseCreateRequest;
import com.example.fivechef.WebChef.dto.CourseResponse;
import com.example.fivechef.WebChef.dto.CourseUpdateRequest;
import com.example.fivechef.WebChef.entity.Course;
import com.example.fivechef.WebChef.entity.CourseCategory;
import com.example.fivechef.WebChef.entity.CourseStatus;
import com.example.fivechef.WebChef.entity.SubscriptionPlanType;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.CourseRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CourseService {

    private final CourseRepository courseRepository;

    private final UserService userService;

    private final CoursePlanPolicyService coursePlanPolicyService;

    private final SubscriptionService subscriptionService;

    // 기존 Controller가 getCourses(page, keyword)를 호출해도 에러 안 나게 유지
    @Transactional(readOnly = true)
    public Page<CourseResponse> getCourses(
            int page,
            String keyword
    ) {
        return getCourses(page, keyword, null, null);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getCourses(
            int page,
            String keyword,
            CourseCategory category,
            String username
    ) {
        Pageable pageable = PageRequest.of(
                page,
                12,
                Sort.by(Sort.Order.desc("id"))
        );

        SubscriptionPlanType userPlan = getUserPlan(username);

        return courseRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    criteriaBuilder.equal(
                            root.get("status"),
                            CourseStatus.OPEN
                    )
            );

            if (category != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("category"),
                                category
                        )
                );
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword.trim() + "%";

                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(root.get("title"), likeKeyword),
                                criteriaBuilder.like(root.get("description"), likeKeyword)
                        )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(course -> toListResponse(course, username, userPlan));
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseResponse(Long id) {
        Course course = getCourseEntity(id);
        return new CourseResponse(course);
    }

    @Transactional(readOnly = true)
    public Course getCourseEntity(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));
    }

    @Transactional
    public void createCourse(
            CourseCreateRequest request,
            String username
    ) {
        validateCreateRequest(request);

        User instructor = userService.getLoginUserEntity(username);

        int price = request.getPrice() == null ? 0 : request.getPrice();

        SubscriptionPlanType requiredPlanType =
                coursePlanPolicyService.decideRequiredPlan(price);

        Course course = new Course();
        course.setTitle(request.getTitle().trim());
        course.setDescription(request.getDescription().trim());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setPrice(price);
        course.setRequiredPlanType(requiredPlanType);
        course.setCategory(request.getCategory());
        course.setDifficulty(request.getDifficulty());
        course.setStatus(request.getStatus() == null ? CourseStatus.DRAFT : request.getStatus());
        course.setInstructor(instructor);

        courseRepository.save(course);
    }

    @Transactional
    public void updateCourse(
            Long id,
            CourseUpdateRequest request
    ) {
        validateUpdateRequest(request);

        Course course = getCourseEntity(id);

        int price = request.getPrice() == null ? 0 : request.getPrice();

        SubscriptionPlanType requiredPlanType =
                coursePlanPolicyService.decideRequiredPlan(price);

        course.setTitle(request.getTitle().trim());
        course.setDescription(request.getDescription().trim());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setPrice(price);
        course.setRequiredPlanType(requiredPlanType);
        course.setCategory(request.getCategory());
        course.setDifficulty(request.getDifficulty());
        course.setStatus(request.getStatus() == null ? CourseStatus.DRAFT : request.getStatus());

        courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseEntity(id);
        courseRepository.delete(course);
    }

    private SubscriptionPlanType getUserPlan(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        User user = userService.getLoginUserEntity(username);

        return subscriptionService.getCurrentPlan(user);
    }

    private CourseResponse toListResponse(
            Course course,
            String username,
            SubscriptionPlanType userPlan
    ) {
        SubscriptionPlanType requiredPlan = course.getRequiredPlanType();

        // 비로그인 사용자는 무료/유료 상관없이 로그인 페이지로 이동
        if (username == null || username.trim().isEmpty()) {
            return new CourseResponse(
                    course,
                    "/user/login",
                    "로그인 후 강의를 이용할 수 있습니다.",
                    false
            );
        }

        // 로그인 사용자 + 무료 강의
        if (requiredPlan == null) {
            return new CourseResponse(
                    course,
                    "/course/detail/" + course.getId(),
                    "무료 강의입니다.",
                    true
            );
        }

        boolean canAccess = coursePlanPolicyService.canAccess(
                userPlan,
                requiredPlan
        );

        if (canAccess) {
            return new CourseResponse(
                    course,
                    "/course/detail/" + course.getId(),
                    "현재 구독권으로 수강 가능합니다.",
                    true
            );
        }

        if (userPlan == SubscriptionPlanType.BASIC
                && requiredPlan == SubscriptionPlanType.PREMIUM) {
            return new CourseResponse(
                    course,
                    "/payment/course/" + course.getId(),
                    "PREMIUM 구독권이 필요한 강의입니다.",
                    false
            );
        }

        return new CourseResponse(
                course,
                "/payment/course/" + course.getId(),
                requiredPlan.name() + " 구독권이 필요한 강의입니다.",
                false
        );
    }

    private void validateCreateRequest(CourseCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("강의 등록 정보가 없습니다.");
        }

        if (isBlank(request.getTitle())) {
            throw new IllegalArgumentException("강의 제목을 입력해주세요.");
        }

        if (isBlank(request.getDescription())) {
            throw new IllegalArgumentException("강의 설명을 입력해주세요.");
        }

        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new IllegalArgumentException("강의 가격은 0원 이상이어야 합니다.");
        }

        if (request.getCategory() == null) {
            throw new IllegalArgumentException("카테고리를 선택해주세요.");
        }

        if (request.getDifficulty() == null) {
            throw new IllegalArgumentException("난이도를 선택해주세요.");
        }
    }

    private void validateUpdateRequest(CourseUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("강의 수정 정보가 없습니다.");
        }

        if (isBlank(request.getTitle())) {
            throw new IllegalArgumentException("강의 제목을 입력해주세요.");
        }

        if (isBlank(request.getDescription())) {
            throw new IllegalArgumentException("강의 설명을 입력해주세요.");
        }

        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new IllegalArgumentException("강의 가격은 0원 이상이어야 합니다.");
        }

        if (request.getCategory() == null) {
            throw new IllegalArgumentException("카테고리를 선택해주세요.");
        }

        if (request.getDifficulty() == null) {
            throw new IllegalArgumentException("난이도를 선택해주세요.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
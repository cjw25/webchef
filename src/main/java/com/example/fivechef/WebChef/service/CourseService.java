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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserService userService;
    private final CoursePlanPolicyService coursePlanPolicyService;
    private final SubscriptionService subscriptionService;

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Value("${file.course-upload-dir:uploads/course}")
    private String uploadDir;

    private static final List<String> ALLOWED_VIDEO_EXTENSIONS = List.of("mp4", "webm", "mov");
    private static final long MAX_VIDEO_SIZE = 500 * 1024 * 1024;

    @Value("${file.course-video-dir:uploads/course/video}")
    private String videoUploadDir;

    @Transactional(readOnly = true)
    public Page<CourseResponse> getCourses(int page, String keyword) {
        return getCourses(page, keyword, null, null, null);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getCourses(
            int page,
            String keyword,
            CourseCategory category,
            String username
    ) {
        return getCourses(page, keyword, category, username, null);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getCourses(
            int page,
            String keyword,
            CourseCategory category,
            String username,
            String sort
    ) {
        Sort sortOption;

        if ("difficulty".equals(sort)) {
            sortOption = Sort.by(Sort.Order.asc("difficultyOrder"));
        } else {
            sortOption = Sort.by(Sort.Order.desc("viewCount"));
        }

        Pageable pageable = PageRequest.of(page, 12, sortOption);

        SubscriptionPlanType userPlan = getUserPlan(username);

        return courseRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("status"), CourseStatus.OPEN));

            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword.trim() + "%";

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("title"), likeKeyword),
                        criteriaBuilder.like(root.get("description"), likeKeyword)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(course -> toListResponse(course, username, userPlan));
    }

    @Transactional
    public CourseResponse getCourseDetailResponse(Long id) {
        Course course = getCourseEntity(id);

        if (course.getViewCount() == null) {
            course.setViewCount(0);
        }

        course.setViewCount(course.getViewCount() + 1);

        return new CourseResponse(course);
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
            String username,
            MultipartFile img,
            MultipartFile video
    ) {
        validateCreateRequest(request);

        User instructor = userService.getLoginUserEntity(username);

        int price = request.getPrice() == null ? 0 : request.getPrice();
        SubscriptionPlanType requiredPlanType = coursePlanPolicyService.decideRequiredPlan(price);

        Course course = new Course();
        course.setTitle(request.getTitle().trim());
        course.setDescription(request.getDescription().trim());
        course.setPrice(price);
        course.setRequiredPlanType(requiredPlanType);
        course.setCategory(request.getCategory());
        course.setDifficulty(request.getDifficulty());
        course.setCookTime(trimOrNull(request.getCookTime()));
        course.setStatus(request.getStatus() == null ? CourseStatus.OPEN : request.getStatus());
        course.setInstructor(instructor);

        saveThumbnail(course, img);

        if (video != null && !video.isEmpty()){
            saveVideo(course, video);
        } else if (!isBlank(request.getVideoUrl())) {
            course.setVideoUrl(request.getVideoUrl().trim());
        }

        courseRepository.save(course);
    }

    @Transactional
    public void updateCourse(Long id, CourseUpdateRequest request, MultipartFile img, MultipartFile video) {
        validateUpdateRequest(request);

        Course course = getCourseEntity(id);

        int price = request.getPrice() == null ? 0 : request.getPrice();
        SubscriptionPlanType requiredPlanType = coursePlanPolicyService.decideRequiredPlan(price);

        course.setTitle(request.getTitle().trim());
        course.setDescription(request.getDescription().trim());
        course.setPrice(price);
        course.setRequiredPlanType(requiredPlanType);
        course.setCategory(request.getCategory());
        course.setDifficulty(request.getDifficulty());
        course.setStatus(request.getStatus() == null ? CourseStatus.DRAFT : request.getStatus());

        if (img !=null && !img.isEmpty()){
            saveThumbnail(course, img);
        }

        if (video != null && !video.isEmpty()){
            saveVideo(course, video);
        } else if (!isBlank(request.getVideoUrl())){
            course.setVideoUrl(request.getVideoUrl().trim());
        }

        courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseEntity(id);
        courseRepository.delete(course);
    }

    private SubscriptionPlanType getUserPlan(String username) {
        if (isBlank(username)) {
            return null;
        }

        User user = userService.getLoginUserEntity(username);
        return subscriptionService.getCurrentPlan(user);
    }

    private CourseResponse toListResponse(Course course, String username, SubscriptionPlanType userPlan) {
        SubscriptionPlanType requiredPlan = course.getRequiredPlanType();

        if (isBlank(username)) {
            return new CourseResponse(
                    course,
                    "/user/login",
                    "로그인 후 강의를 이용할 수 있습니다.",
                    false
            );
        }

        if (requiredPlan == null) {
            return new CourseResponse(
                    course,
                    "/course/view/" + course.getId(),
                    "무료 강의입니다.",
                    true
            );
        }

        boolean canAccess = coursePlanPolicyService.canAccess(userPlan, requiredPlan);

        if (canAccess) {
            return new CourseResponse(
                    course,
                    "/course/view/" + course.getId(),
                    "현재 구독권으로 수강 가능합니다.",
                    true
            );
        }

        if (userPlan == SubscriptionPlanType.BASIC && requiredPlan == SubscriptionPlanType.PREMIUM) {
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

    private void saveThumbnail(Course course, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("음식 사진을 첨부해주세요.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("사진 용량은 10MB를 초과할 수 없습니다.");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("잘못된 파일입니다.");
        }

        int dotIdx = originalFileName.lastIndexOf(".");

        if (dotIdx == -1 || dotIdx == originalFileName.length() - 1) {
            throw new IllegalArgumentException("확장자가 없는 파일은 업로드할 수 없습니다.");
        }

        String extension = originalFileName.substring(dotIdx + 1).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("이미지 파일(jpg, jpeg, png, gif, webp)만 업로드할 수 있습니다.");
        }

        try {
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            String storedFileName = java.util.UUID.randomUUID() + "." + extension;
            java.nio.file.Path filePath = uploadPath.resolve(storedFileName);

            file.transferTo(filePath.toFile());

            course.setThumbnailUrl("/uploads/course/" + storedFileName);

        } catch (Exception e) {
            throw new IllegalArgumentException("파일 저장 중 오류가 발생했습니다.");
        }
    }

    private void saveVideo(Course course, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new IllegalArgumentException("영상 용량은 500MB를 초과할 수 없습니다.");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("잘못된 파일입니다.");
        }

        int dotIdx = originalFileName.lastIndexOf(".");

        if (dotIdx == -1 || dotIdx == originalFileName.length() - 1) {
            throw new IllegalArgumentException("확장자가 없는 파일은 업로드할 수 없습니다.");
        }

        String extension = originalFileName.substring(dotIdx + 1).toLowerCase();

        if (!ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("영상 파일(mp4, webm, mov)만 업로드할 수 있습니다.");
        }

        try {
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(videoUploadDir)
                    .toAbsolutePath()
                    .normalize();

            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            String storedFileName = java.util.UUID.randomUUID() + "." + extension;
            java.nio.file.Path filePath = uploadPath.resolve(storedFileName);

            file.transferTo(filePath.toFile());

            course.setVideoUrl("/uploads/course/video/" + storedFileName);

        } catch (Exception e) {
            throw new IllegalArgumentException("영상 저장 중 오류가 발생했습니다.");
        }
    }

    private String trimOrNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
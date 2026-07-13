package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.CourseCreateRequest;
import com.example.fivechef.WebChef.dto.CourseResponse;
import com.example.fivechef.WebChef.dto.CourseUpdateRequest;
import com.example.fivechef.WebChef.entity.*;
import com.example.fivechef.WebChef.repository.CourseRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        Sort sortOption = "difficulty".equals(sort)
                ? Sort.by(Sort.Order.asc("difficultyOrder"))
                : Sort.by(Sort.Order.desc("viewCount"));

        Pageable pageable = PageRequest.of(page, 12, sortOption);

        User loginUser = getLoginUserOrNull(username);
        SubscriptionPlanType userPlan = loginUser == null ? null : subscriptionService.getCurrentPlan(loginUser);

        return courseRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // USER 강의 목록에는 관리자 승인 완료된 강의만 노출
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
        }, pageable).map(course -> toListResponse(course, loginUser, userPlan));
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getInstructorCourses(String username) {
        User instructor = userService.getLoginUserEntity(username);

        return courseRepository.findByInstructorOrderByIdDesc(instructor)
                .stream()
                .map(CourseResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAdminReviewCourses() {
        return courseRepository.findByStatusInOrderByIdDesc(
                        List.of(CourseStatus.PENDING, CourseStatus.UPDATE_PENDING)
                )
                .stream()
                .map(CourseResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseResponse(Long id) {
        Course course = getCourseEntity(id);
        return new CourseResponse(course);
    }

    @Transactional
    public CourseResponse getCourseDetailResponse(Long id) {
        Course course = getCourseEntity(id);
        course.setViewCount(course.getViewCount() + 1);
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
            MultipartFile img
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
        course.setVideoUrl(convertYoutubeUrlToEmbedUrl(request.getVideoUrl()));
        course.setStatus(CourseStatus.PENDING);
        course.setInstructor(instructor);

        saveThumbnail(course, img);

        courseRepository.save(course);
    }

    @Transactional
    public void updateCourse(
            Long id,
            CourseUpdateRequest request,
            String username,
            MultipartFile img
    ) {
        validateUpdateRequest(request);

        Course course = getCourseEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        validateCourseOwnerOrAdmin(course, loginUser);

        int price = request.getPrice() == null ? 0 : request.getPrice();
        SubscriptionPlanType requiredPlanType = coursePlanPolicyService.decideRequiredPlan(price);

        course.setTitle(request.getTitle().trim());
        course.setDescription(request.getDescription().trim());
        course.setPrice(price);
        course.setRequiredPlanType(requiredPlanType);
        course.setCategory(request.getCategory());
        course.setDifficulty(request.getDifficulty());
        course.setCookTime(trimOrNull(request.getCookTime()));
        course.setVideoUrl(convertYoutubeUrlToEmbedUrl(request.getVideoUrl()));

        if (img != null && !img.isEmpty()) {
            saveThumbnail(course, img);
        }

        // 관리자가 수정하면 바로 공개, 강사가 수정하면 다시 승인대기
        if (loginUser.getRole() == Role.ADMIN) {
            course.setStatus(CourseStatus.OPEN);
        } else {
            course.setStatus(CourseStatus.UPDATE_PENDING);
        }

        courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id, String username) {
        Course course = getCourseEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        validateCourseOwnerOrAdmin(course, loginUser);

        courseRepository.delete(course);
    }

    @Transactional
    public void approveCourse(Long id) {
        Course course = getCourseEntity(id);
        course.setStatus(CourseStatus.OPEN);
        courseRepository.save(course);
    }

    @Transactional
    public void rejectCourse(Long id) {
        Course course = getCourseEntity(id);
        course.setStatus(CourseStatus.REJECTED);
        courseRepository.save(course);
    }

    private CourseResponse toListResponse(
            Course course,
            User loginUser,
            SubscriptionPlanType userPlan
    ) {
        SubscriptionPlanType requiredPlan = course.getRequiredPlanType();

        if (loginUser == null) {
            return new CourseResponse(
                    course,
                    "/user/login",
                    "로그인 후 강의를 이용할 수 있습니다.",
                    false
            );
        }

        if (loginUser.getRole() == Role.ADMIN) {
            return new CourseResponse(
                    course,
                    "/course/view/" + course.getId(),
                    "관리자는 결제 없이 강의를 확인할 수 있습니다.",
                    true
            );
        }

        if (loginUser.getRole() == Role.INSTRUCTOR
                && course.getInstructor() != null
                && course.getInstructor().getId().equals(loginUser.getId())) {
            return new CourseResponse(
                    course,
                    "/course/view/" + course.getId(),
                    "본인이 등록한 강의입니다.",
                    true
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

        return new CourseResponse(
                course,
                "/payment/course/" + course.getId(),
                requiredPlan.name() + " 구독권이 필요한 강의입니다.",
                false
        );
    }

    private void validateCourseOwnerOrAdmin(Course course, User loginUser) {
        if (loginUser.getRole() == Role.ADMIN) {
            return;
        }

        if (course.getInstructor() == null
                || !course.getInstructor().getId().equals(loginUser.getId())) {
            throw new IllegalArgumentException("본인이 등록한 강의만 수정/삭제할 수 있습니다.");
        }
    }

    private User getLoginUserOrNull(String username) {
        if (isBlank(username)) {
            return null;
        }

        return userService.getLoginUserEntity(username);
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

        if (isBlank(request.getCookTime())) {
            throw new IllegalArgumentException("조리 시간을 입력해주세요.");
        }

        if (isBlank(request.getVideoUrl())) {
            throw new IllegalArgumentException("유튜브 영상 URL을 입력해주세요.");
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

        if (isBlank(request.getCookTime())) {
            throw new IllegalArgumentException("조리 시간을 입력해주세요.");
        }

        if (isBlank(request.getVideoUrl())) {
            throw new IllegalArgumentException("유튜브 영상 URL을 입력해주세요.");
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

            String storedFileName = UUID.randomUUID() + "." + extension;
            java.nio.file.Path filePath = uploadPath.resolve(storedFileName);

            file.transferTo(filePath.toFile());

            course.setThumbnailUrl("/uploads/course/" + storedFileName);

        } catch (Exception e) {
            throw new IllegalArgumentException("파일 저장 중 오류가 발생했습니다.");
        }
    }

    private String convertYoutubeUrlToEmbedUrl(String url) {
        if (isBlank(url)) {
            return null;
        }

        String trimmedUrl = url.trim();

        if (trimmedUrl.contains("youtube.com/embed/")) {
            return trimmedUrl;
        }

        String videoId = extractYoutubeVideoId(trimmedUrl);

        if (isBlank(videoId)) {
            throw new IllegalArgumentException("올바른 유튜브 공유 URL을 입력해주세요.");
        }

        return "https://www.youtube.com/embed/" + videoId;
    }

    private String extractYoutubeVideoId(String url) {
        String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);

        if (decodedUrl.contains("youtu.be/")) {
            String id = decodedUrl.substring(decodedUrl.indexOf("youtu.be/") + "youtu.be/".length());
            return cleanYoutubeId(id);
        }

        if (decodedUrl.contains("youtube.com/watch")) {
            String query = decodedUrl.substring(decodedUrl.indexOf("?") + 1);
            String[] params = query.split("&");

            for (String param : params) {
                String[] pair = param.split("=");

                if (pair.length == 2 && "v".equals(pair[0])) {
                    return cleanYoutubeId(pair[1]);
                }
            }
        }

        if (decodedUrl.contains("youtube.com/shorts/")) {
            String id = decodedUrl.substring(decodedUrl.indexOf("youtube.com/shorts/") + "youtube.com/shorts/".length());
            return cleanYoutubeId(id);
        }

        return null;
    }

    private String cleanYoutubeId(String value) {
        if (isBlank(value)) {
            return null;
        }

        String result = value.trim();

        if (result.contains("?")) {
            result = result.substring(0, result.indexOf("?"));
        }

        if (result.contains("&")) {
            result = result.substring(0, result.indexOf("&"));
        }

        if (result.contains("/")) {
            result = result.substring(0, result.indexOf("/"));
        }

        return result;
    }

    private String trimOrNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
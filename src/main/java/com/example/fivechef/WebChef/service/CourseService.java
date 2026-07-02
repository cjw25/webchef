package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.CourseCreateRequest;
import com.example.fivechef.WebChef.dto.CourseResponse;
import com.example.fivechef.WebChef.dto.CourseUpdateRequest;
import com.example.fivechef.WebChef.entity.Course;
import com.example.fivechef.WebChef.entity.CourseStatus;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Course getCourseEntity(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getCourses(int page, String keyword) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Order.desc("id"))
        );

        if (isBlank(keyword)) {
            return courseRepository.findAll(pageable)
                    .map(CourseResponse::new);
        }

        String kw = keyword.trim();

        return courseRepository.findByTitleContainingOrDescriptionContaining(
                        kw,
                        kw,
                        pageable
                )
                .map(CourseResponse::new);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getOpenCourses(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("id"))
        );

        return courseRepository.findByStatus(CourseStatus.OPEN, pageable)
                .map(CourseResponse::new);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseResponse(Long id) {
        return new CourseResponse(getCourseEntity(id));
    }

    @Transactional
    public void createCourse(CourseCreateRequest request, String username) {
        validateCreateRequest(request);

        User instructor = userService.getLoginUserEntity(username);

        Course course = new Course();
        course.setTitle(request.getTitle().trim());
        course.setDescription(request.getDescription().trim());
        course.setThumbnailUrl(trimOrNull(request.getThumbnailUrl()));
        course.setPrice(request.getPrice() == null ? 0 : request.getPrice());
        course.setCategory(request.getCategory());
        course.setDifficulty(request.getDifficulty());
        course.setStatus(request.getStatus() == null ? CourseStatus.DRAFT : request.getStatus());
        course.setInstructor(instructor);

        courseRepository.save(course);
    }

    @Transactional
    public void updateCourse(Long id, CourseUpdateRequest request) {
        validateUpdateRequest(request);

        Course course = getCourseEntity(id);
        course.setTitle(request.getTitle().trim());
        course.setDescription(request.getDescription().trim());
        course.setThumbnailUrl(trimOrNull(request.getThumbnailUrl()));
        course.setPrice(request.getPrice() == null ? 0 : request.getPrice());
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

        if (request.getCategory() == null) {
            throw new IllegalArgumentException("강의 카테고리를 선택해주세요.");
        }

        if (request.getDifficulty() == null) {
            throw new IllegalArgumentException("강의 난이도를 선택해주세요.");
        }

        if (request.getPrice() != null && request.getPrice() < 0) {
            throw new IllegalArgumentException("강의 가격은 0원 이상이어야 합니다.");
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

        if (request.getCategory() == null) {
            throw new IllegalArgumentException("강의 카테고리를 선택해주세요.");
        }

        if (request.getDifficulty() == null) {
            throw new IllegalArgumentException("강의 난이도를 선택해주세요.");
        }

        if (request.getPrice() != null && request.getPrice() < 0) {
            throw new IllegalArgumentException("강의 가격은 0원 이상이어야 합니다.");
        }
    }

    private String trimOrNull(String value) {
        if (isBlank(value)) {
            return null;
        }

        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
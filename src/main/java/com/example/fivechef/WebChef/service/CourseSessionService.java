package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.CourseSessionRequest;
import com.example.fivechef.WebChef.dto.CourseSessionResponse;
import com.example.fivechef.WebChef.entity.Course;
import com.example.fivechef.WebChef.entity.CourseSession;
import com.example.fivechef.WebChef.repository.CourseSessionRepository;
import com.example.fivechef.WebChef.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CourseSessionService {

    private final CourseSessionRepository courseSessionRepository;
    private final QuizRepository quizRepository;
    private final CourseService courseService;

    @Transactional(readOnly = true)
    public CourseSession getSessionEntity(Long id) {
        return courseSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("차시를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<CourseSessionResponse> getSessions(Long courseId) {
        List<CourseSession> sessions = courseSessionRepository.findByCourseIdOrderBySortOrderAsc(courseId);

        return sessions.stream()
                .map(session -> new CourseSessionResponse(
                        session,
                        quizRepository.findBySessionId(session.getId()).isPresent()
                ))
                .toList();
    }

    @Transactional
    public void createSession(Long courseId, CourseSessionRequest request) {
        validateRequest(request);

        Course course = courseService.getCourseEntity(courseId);

        CourseSession session = new CourseSession();
        session.setCourse(course);
        session.setTitle(request.getTitle().trim());
        session.setVideoUrl(trimOrNull(request.getVideoUrl()));
        session.setSortOrder(request.getSortOrder());

        courseSessionRepository.save(session);
    }

    @Transactional
    public void updateSession(Long id, CourseSessionRequest request) {
        validateRequest(request);

        CourseSession session = getSessionEntity(id);
        session.setTitle(request.getTitle().trim());
        session.setVideoUrl(trimOrNull(request.getVideoUrl()));
        session.setSortOrder(request.getSortOrder());

        courseSessionRepository.save(session);
    }

    @Transactional
    public void deleteSession(Long id) {
        CourseSession session = getSessionEntity(id);
        courseSessionRepository.delete(session);
    }

    private void validateRequest(CourseSessionRequest request) {
        if (request == null || isBlank(request.getTitle())) {
            throw new IllegalArgumentException("차시 제목을 입력해주세요.");
        }
    }

    private String trimOrNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Transactional(readOnly = true)
    public List<CourseSession> getSessionEntitiesByCourseId(Long courseId) {
        return courseSessionRepository.findByCourseIdOrderBySortOrderAsc(courseId);
    }
}

package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.CourseCommentCreateRequest;
import com.example.fivechef.WebChef.dto.CourseCommentResponse;
import com.example.fivechef.WebChef.entity.Course;
import com.example.fivechef.WebChef.entity.CourseComment;
import com.example.fivechef.WebChef.entity.CourseCommentImage;
import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.CourseCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CourseCommentService {

    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int MAX_FILE_COUNT = 3;

    private final CourseCommentRepository courseCommentRepository;
    private final CourseService courseService;
    private final UserService userService;

    private final QuizService quizService;

    @Value("${file.course-comment-upload-dir:uploads/course-comment}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public CourseComment getCommentEntity(Long id) {
        return courseCommentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<CourseCommentResponse> getComments(Long courseId, int page) {
        Pageable pageable = PageRequest.of(page, 10);

        return courseCommentRepository.findByCourseIdOrderByCreateDateDesc(courseId, pageable)
                .map(CourseCommentResponse::new);
    }

    @Transactional
    public void createComment(CourseCommentCreateRequest request, String username, MultipartFile[] img) {
        validateRequest(request);

        Course course = courseService.getCourseEntity(request.getCourseId());
        User author = userService.getLoginUserEntity(username);

        CourseComment comment = new CourseComment();
        comment.setCourse(course);
        comment.setAuthor(author);
        comment.setContent(request.getContent().trim());

        courseCommentRepository.save(comment);

        saveImages(comment, img);

        if(quizService.hasPassedCourseQuiz(author.getId(), course.getId())) {
            author.setPoint(author.getPoint() + 100);
        }
    }

    @Transactional
    public void deleteComment(Long id, String username) {
        CourseComment comment = getCommentEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        boolean isAdmin = loginUser.getRole() == Role.ADMIN;
        boolean isOwner = comment.getAuthor() != null && comment.getAuthor().getId().equals(loginUser.getId());

        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        deleteStoredImages(comment);

        courseCommentRepository.delete(comment);
    }

    private void saveImages(CourseComment comment, MultipartFile[] image) {
        if (image == null) {
            return;
        }

        List<MultipartFile> validFiles = new ArrayList<>();
        for (MultipartFile file : image) {
            if (file != null && !file.isEmpty()) {
                validFiles.add(file);
            }
        }

        if (validFiles.isEmpty()) {
            return;
        }

        if (validFiles.size() > MAX_FILE_COUNT) {
            throw new IllegalArgumentException("사진은 최대 " + MAX_FILE_COUNT + "개까지 첨부할 수 있어요.");
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("업로드 폴더 생성 중 오류가 발생했습니다.");
        }

        for (int i = 0; i < validFiles.size(); i++) {
            MultipartFile file = validFiles.get(i);

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("사진 용량은 10MB를 초과할 수 없어요.");
            }

            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || originalFileName.isBlank()) {
                throw new IllegalArgumentException("잘못된 파일입니다.");
            }

            int dotIdx = originalFileName.lastIndexOf(".");
            if (dotIdx == -1 || dotIdx == originalFileName.length() - 1) {
                throw new IllegalArgumentException("확장자가 없는 파일은 업로드할 수 없어요.");
            }
            String extension = originalFileName.substring(dotIdx + 1).toLowerCase();

            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException("이미지 파일(jpg, jpeg, png, gif, webp)만 업로드할 수 있어요.");
            }

            String storedFileName = UUID.randomUUID() + "." + extension;
            Path filePath = uploadPath.resolve(storedFileName);

            try {
                file.transferTo(filePath.toFile());
            } catch (Exception e) {
                throw new IllegalArgumentException("파일 저장 중 오류가 발생했습니다.");
            }

            CourseCommentImage commentImage = new CourseCommentImage();
            commentImage.setComment(comment);
            commentImage.setStoredFileName(storedFileName);
            commentImage.setFileUrl("/uploads/course-comment/" + storedFileName);
            commentImage.setSortOrder(i);

            comment.getImages().add(commentImage);
        }
    }

    private void deleteStoredImages(CourseComment comment) {
        if (comment.getImages() == null) {
            return;
        }

        for (CourseCommentImage image : comment.getImages()) {
            try {
                Path filePath = Paths.get(uploadDir)
                        .toAbsolutePath()
                        .normalize()
                        .resolve(image.getStoredFileName());

                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                // 개별 파일 삭제 실패는 무시
            }
        }
    }

    private void validateRequest(CourseCommentCreateRequest request) {
        if (request == null || request.getCourseId() == null) {
            throw new IllegalArgumentException("강의 정보가 없습니다.");
        }

        if (isBlank(request.getContent())) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.InquiryCreateRequest;
import com.example.fivechef.WebChef.dto.InquiryResponse;
import com.example.fivechef.WebChef.dto.InquiryUpdateRequest;
import com.example.fivechef.WebChef.entity.Inquiry;
import com.example.fivechef.WebChef.entity.InquiryImage;
import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class InquiryService {

    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int MAX_FILE_COUNT = 3;

    private final InquiryRepository inquiryRepository;
    private final UserService userService;

    @Value("${file.inquiry-upload-dir:uploads/inquiry}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public Inquiry getInquiryEntity(Long id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문의사항을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<InquiryResponse> getInquiries(int page, String keyword) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Order.desc("id"))
        );

        if (isBlank(keyword)) {
            return inquiryRepository.findAll(pageable)
                    .map(InquiryResponse::new);
        }

        String kw = keyword.trim();

        return inquiryRepository.findBySubjectContainingOrContentContaining(
                        kw,
                        kw,
                        pageable
                )
                .map(InquiryResponse::new);
    }

    // 상세 조회 시 조회수 1 증가
    @Transactional
    public InquiryResponse getInquiryResponse(Long id, String username) {
        Inquiry inquiry = getInquiryEntity(id);
        inquiry.setViewCount(inquiry.getViewCount() + 1);

        InquiryResponse response = new InquiryResponse(inquiry);

        boolean isMine = username != null
                && inquiry.getAuthor() != null
                && username.equals(inquiry.getAuthor().getUsername());

        response.setMine(isMine);

        return response;
    }

    @Transactional
    public void createInquiry(InquiryCreateRequest request, String username, MultipartFile[] img) {
        validateCreateRequest(request);

        User author = userService.getLoginUserEntity(username);

        Inquiry inquiry = new Inquiry();
        inquiry.setSubject(request.getSubject().trim());
        inquiry.setContent(request.getContent().trim());
        inquiry.setAuthor(author);
        inquiry.setAnswered(false);

        inquiryRepository.save(inquiry);

        saveImages(inquiry, img);
    }

    @Transactional
    public void updateInquiry(Long id, InquiryUpdateRequest request, String username, MultipartFile[] img) {
        validateUpdateRequest(request);

        Inquiry inquiry = getInquiryEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        checkOwnerOrAdmin(inquiry, loginUser, "수정 권한이 없습니다.");

        inquiry.setSubject(request.getSubject().trim());
        inquiry.setContent(request.getContent().trim());

        removeImages(inquiry, request.getDeleteImageIds());
        appendImages(inquiry, img);

        inquiryRepository.save(inquiry);
    }

    @Transactional
    public void answerInquiry(Long id, String answerContent) {
        if (isBlank(answerContent)) {
            throw new IllegalArgumentException("답변 내용을 입력해주세요.");
        }

        Inquiry inquiry = getInquiryEntity(id);
        inquiry.setAnswerContent(answerContent.trim());
        inquiry.setAnswered(true);
        inquiry.setAnswerDate(LocalDateTime.now());

        inquiryRepository.save(inquiry);
    }

    @Transactional
    public void deleteInquiry(Long id, String username) {
        Inquiry inquiry = getInquiryEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        checkOwnerOrAdmin(inquiry, loginUser, "삭제 권한이 없습니다.");

        deleteStoredImages(inquiry);

        inquiryRepository.delete(inquiry);
    }

    private void saveImages(Inquiry inquiry, MultipartFile[] image) {
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

            InquiryImage inquiryImage = new InquiryImage();
            inquiryImage.setInquiry(inquiry);
            inquiryImage.setStoredFileName(storedFileName);
            inquiryImage.setFileUrl("/uploads/inquiry/" + storedFileName);
            inquiryImage.setSortOrder(i);

            inquiry.getImages().add(inquiryImage);
        }
    }

    private void removeImages(Inquiry inquiry, List<Long> deleteImageIds) {
        if (deleteImageIds == null || deleteImageIds.isEmpty()) {
            return;
        }

        List<InquiryImage> toRemove = inquiry.getImages().stream()
                .filter(img -> deleteImageIds.contains(img.getId()))
                .toList();

        for (InquiryImage img : toRemove) {
            try {
                Path filePath = Paths.get(uploadDir)
                        .toAbsolutePath()
                        .normalize()
                        .resolve(img.getStoredFileName());
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                // 개별 파일 삭제 실패는 무시
            }
        }

        inquiry.getImages().removeAll(toRemove);
    }

    private void appendImages(Inquiry inquiry, MultipartFile[] image) {
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

        int existingCount = inquiry.getImages().size();

        if (existingCount + validFiles.size() > MAX_FILE_COUNT) {
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

        int nextSortOrder = inquiry.getImages().stream()
                .mapToInt(InquiryImage::getSortOrder)
                .max()
                .orElse(-1) + 1;

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

            InquiryImage inquiryImage = new InquiryImage();
            inquiryImage.setInquiry(inquiry);
            inquiryImage.setStoredFileName(storedFileName);
            inquiryImage.setFileUrl("/uploads/inquiry/" + storedFileName);
            inquiryImage.setSortOrder(nextSortOrder + i);

            inquiry.getImages().add(inquiryImage);
        }
    }

    private void deleteStoredImages(Inquiry inquiry) {
        if (inquiry.getImages() == null) {
            return;
        }

        for (InquiryImage image : inquiry.getImages()) {
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

    private void validateCreateRequest(InquiryCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("문의 등록 정보가 없습니다.");
        }

        if (isBlank(request.getSubject())) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }

        if (isBlank(request.getContent())) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
    }

    private void validateUpdateRequest(InquiryUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("문의 수정 정보가 없습니다.");
        }

        if (isBlank(request.getSubject())) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }

        if (isBlank(request.getContent())) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
    }

    private void checkOwnerOrAdmin(Inquiry inquiry, User loginUser, String message) {
        boolean isAdmin = loginUser.getRole() == Role.ADMIN;
        boolean isOwner = inquiry.getAuthor() != null
                && inquiry.getAuthor().getId().equals(loginUser.getId());

        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.NoticeCreateRequest;
import com.example.fivechef.WebChef.dto.NoticeResponse;
import com.example.fivechef.WebChef.dto.NoticeUpdateRequest;
import com.example.fivechef.WebChef.entity.Notice;
import com.example.fivechef.WebChef.entity.NoticeImage;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.NoticeRepository;
import com.example.fivechef.WebChef.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserService userService;

    @Value("${file.notice-upload-dir:uploads/notice}")
    private String noticeUploadDir;

    @Transactional(readOnly = true)
    public Notice getNoticeEntity(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "공지사항을 찾을 수 없습니다."
                        )
                );
    }

    @Transactional(readOnly = true)
    public Page<NoticeResponse> getNotices(
            int page,
            String keyword
    ) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Order.desc("id"))
        );

        if (isBlank(keyword)) {
            return noticeRepository.findAll(pageable)
                    .map(NoticeResponse::new);
        }

        String kw = keyword.trim();

        return noticeRepository
                .findBySubjectContainingOrContentContaining(
                        kw,
                        kw,
                        pageable
                )
                .map(NoticeResponse::new);
    }

    @Transactional(readOnly = true)
    public NoticeResponse getNoticeResponse(Long id) {
        Notice notice = getNoticeEntity(id);

        return new NoticeResponse(notice);
    }

    @Transactional
    public NoticeResponse getNoticeDetail(Long id) {
        Notice notice = getNoticeEntity(id);

        notice.setViewCount(notice.getViewCount() + 1);

        return new NoticeResponse(notice);
    }

    @Transactional
    public void createNotice(
            NoticeCreateRequest request,
            String username,
            List<MultipartFile> images
    ) {
        validateCreateRequest(request);

        User author = userService.getLoginUserEntity(username);

        Notice notice = new Notice();
        notice.setSubject(request.getSubject().trim());
        notice.setContent(request.getContent().trim());
        notice.setAuthor(author);

        /*
         * 먼저 공지사항을 영속 상태로 만든다.
         */
        noticeRepository.save(notice);

        /*
         * Notice의 images 컬렉션에 추가하면
         * cascade 설정으로 NoticeImage도 저장된다.
         */
        saveImages(notice, images);
    }

    @Transactional
    public void updateNotice(
            Long id,
            NoticeUpdateRequest request,
            List<MultipartFile> newImages,
            List<Long> deleteImageIds
    ) {
        validateUpdateRequest(request);

        Notice notice = getNoticeEntity(id);

        notice.setSubject(request.getSubject().trim());
        notice.setContent(request.getContent().trim());

        deleteSelectedImages(
                notice,
                deleteImageIds
        );

        saveImages(
                notice,
                newImages
        );

        /*
         * notice는 영속 상태이므로 별도의 save 호출이 없어도
         * 트랜잭션 종료 시 변경 감지로 수정된다.
         */
    }

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = getNoticeEntity(id);

        /*
         * DB 삭제 전에 실제 이미지 파일부터 삭제한다.
         */
        for (NoticeImage image : notice.getImages()) {
            deletePhysicalFile(
                    image.getStoredFileName()
            );
        }

        /*
         * cascade = ALL, orphanRemoval = true이면
         * NoticeImage 데이터도 함께 삭제된다.
         */
        noticeRepository.delete(notice);
    }

    private void saveImages(
            Notice notice,
            List<MultipartFile> images
    ) {
        if (images == null || images.isEmpty()) {
            return;
        }

        Path uploadPath = Paths.get(noticeUploadDir)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "공지사항 이미지 저장 폴더를 생성할 수 없습니다.",
                    e
            );
        }

        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                continue;
            }

            validateImage(image);

            String originalFileName =
                    image.getOriginalFilename();

            String extension =
                    getExtension(originalFileName);

            String storedFileName =
                    UUID.randomUUID() + extension;

            Path targetPath = uploadPath
                    .resolve(storedFileName)
                    .normalize();

            /*
             * 조작된 파일명으로 상위 폴더에 저장되는 것을 방지한다.
             */
            if (!targetPath.startsWith(uploadPath)) {
                throw new IllegalArgumentException(
                        "잘못된 이미지 파일 경로입니다."
                );
            }

            try {
                Files.copy(
                        image.getInputStream(),
                        targetPath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (IOException e) {
                throw new IllegalStateException(
                        "공지사항 이미지 저장에 실패했습니다.",
                        e
                );
            }

            /*
             * 아래 생성자는 NoticeImage 엔티티에 있어야 한다.
             *
             * NoticeImage(
             *     String imageUrl,
             *     String storedFileName,
             *     String originalFileName
             * )
             */
            NoticeImage noticeImage =
                    new NoticeImage(
                            "/uploads/notice/" + storedFileName,
                            storedFileName,
                            originalFileName
                    );

            notice.addImage(noticeImage);
        }
    }

    private void deleteSelectedImages(
            Notice notice,
            List<Long> deleteImageIds
    ) {
        if (deleteImageIds == null ||
                deleteImageIds.isEmpty()) {
            return;
        }

        /*
         * stream 결과를 별도 리스트로 만든 뒤 삭제해야
         * ConcurrentModificationException이 발생하지 않는다.
         */
        List<NoticeImage> deleteTargets =
                notice.getImages()
                        .stream()
                        .filter(image ->
                                image.getId() != null &&
                                        deleteImageIds.contains(
                                                image.getId()
                                        )
                        )
                        .toList();

        for (NoticeImage image : deleteTargets) {
            deletePhysicalFile(
                    image.getStoredFileName()
            );

            notice.removeImage(image);
        }
    }

    private void deletePhysicalFile(
            String storedFileName
    ) {
        if (isBlank(storedFileName)) {
            return;
        }

        Path uploadPath = Paths.get(noticeUploadDir)
                .toAbsolutePath()
                .normalize();

        Path filePath = uploadPath
                .resolve(storedFileName)
                .normalize();

        /*
         * 업로드 폴더 밖의 파일을 삭제하지 못하도록 검사한다.
         */
        if (!filePath.startsWith(uploadPath)) {
            throw new IllegalArgumentException(
                    "잘못된 이미지 파일 경로입니다."
            );
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "공지사항 이미지 파일 삭제에 실패했습니다.",
                    e
            );
        }
    }

    private void validateImage(
            MultipartFile image
    ) {
        String contentType = image.getContentType();

        if (contentType == null ||
                !contentType.startsWith("image/")) {
            throw new IllegalArgumentException(
                    "이미지 파일만 업로드할 수 있습니다."
            );
        }

        long maxSize =
                10L * 1024 * 1024;

        if (image.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    "이미지 한 장의 크기는 10MB를 초과할 수 없습니다."
            );
        }

        String originalFileName =
                image.getOriginalFilename();

        String extension =
                getExtension(originalFileName);

        if (!isAllowedImageExtension(extension)) {
            throw new IllegalArgumentException(
                    "jpg, jpeg, png, gif, webp 이미지만 업로드할 수 있습니다."
            );
        }
    }

    private boolean isAllowedImageExtension(
            String extension
    ) {
        return extension.equals(".jpg") ||
                extension.equals(".jpeg") ||
                extension.equals(".png") ||
                extension.equals(".gif") ||
                extension.equals(".webp");
    }

    private String getExtension(
            String originalFileName
    ) {
        if (isBlank(originalFileName)) {
            return "";
        }

        int dotIndex =
                originalFileName.lastIndexOf('.');

        if (dotIndex < 0) {
            return "";
        }

        return originalFileName
                .substring(dotIndex)
                .toLowerCase();
    }

    private void validateCreateRequest(
            NoticeCreateRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "공지사항 등록 정보가 없습니다."
            );
        }

        if (isBlank(request.getSubject())) {
            throw new IllegalArgumentException(
                    "제목을 입력해주세요."
            );
        }

        if (isBlank(request.getContent())) {
            throw new IllegalArgumentException(
                    "내용을 입력해주세요."
            );
        }
    }

    private void validateUpdateRequest(
            NoticeUpdateRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "공지사항 수정 정보가 없습니다."
            );
        }

        if (isBlank(request.getSubject())) {
            throw new IllegalArgumentException(
                    "제목을 입력해주세요."
            );
        }

        if (isBlank(request.getContent())) {
            throw new IllegalArgumentException(
                    "내용을 입력해주세요."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null ||
                value.trim().isEmpty();
    }
}


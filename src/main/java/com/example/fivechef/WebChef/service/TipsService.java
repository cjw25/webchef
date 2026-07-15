package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.TipResponse;
import com.example.fivechef.WebChef.entity.Tip;
import com.example.fivechef.WebChef.entity.TipCategory;
import com.example.fivechef.WebChef.repository.TipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TipsService {

    private final TipRepository tipRepository;

    @Value("${file.tips-upload-dir:uploads/tips}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public Page<TipResponse> getTips(
            int page,
            String keyword,
            String category
    ) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Order.desc("id"))
        );

        boolean hasKeyword =
                keyword != null && !keyword.trim().isEmpty();

        boolean hasCategory =
                category != null && !category.trim().isEmpty();

        Page<Tip> tips;

        if (hasKeyword && hasCategory) {
            TipCategory tipCategory = parseCategory(category);

            tips = tipRepository
                    .findByCategoryAndTitleContainingOrCategoryAndContentContaining(
                            tipCategory,
                            keyword.trim(),
                            tipCategory,
                            keyword.trim(),
                            pageable
                    );

        } else if (hasCategory) {
            TipCategory tipCategory = parseCategory(category);

            tips = tipRepository.findByCategory(
                    tipCategory,
                    pageable
            );

        } else if (hasKeyword) {
            tips = tipRepository.findByTitleContainingOrContentContaining(
                    keyword.trim(),
                    keyword.trim(),
                    pageable
            );

        } else {
            tips = tipRepository.findAll(pageable);
        }

        return tips.map(TipResponse::new);
    }

    @Transactional
    public TipResponse getTip(Long id) {
        Tip tip = findTip(id);

        Integer viewCount = tip.getViewCount();

        if (viewCount == null) {
            viewCount = 0;
        }

        tip.setViewCount(viewCount + 1);

        return new TipResponse(tip);
    }

    /*
     * 수정 화면 조회용입니다.
     * 조회수를 증가시키지 않습니다.
     */
    @Transactional(readOnly = true)
    public TipResponse getTipForUpdate(Long id) {
        Tip tip = findTip(id);

        return new TipResponse(tip);
    }

    @Transactional
    public Long createTip(
            String title,
            String content,
            TipCategory category,
            MultipartFile imageFile
    ) {
        Tip tip = new Tip();

        tip.setTitle(title);
        tip.setContent(content);
        tip.setCategory(category);
        tip.setImageUrl(storeImage(imageFile));

        return tipRepository.save(tip).getId();
    }

    @Transactional
    public void updateTip(
            Long id,
            String title,
            String content,
            TipCategory category,
            MultipartFile imageFile,
            boolean deleteImage
    ) {
        Tip tip = findTip(id);

        tip.setTitle(title);
        tip.setContent(content);
        tip.setCategory(category);

        String oldImageUrl = tip.getImageUrl();

        /*
         * 기존 이미지 삭제를 선택한 경우
         */
        if (deleteImage) {
            deleteImageFile(oldImageUrl);
            tip.setImageUrl(null);
        }

        /*
         * 새 이미지를 업로드한 경우
         * 기존 이미지를 삭제하고 새 이미지로 교체
         */
        if (imageFile != null && !imageFile.isEmpty()) {
            if (!deleteImage) {
                deleteImageFile(oldImageUrl);
            }

            String newImageUrl = storeImage(imageFile);
            tip.setImageUrl(newImageUrl);
        }
    }

    @Transactional
    public void deleteTip(Long id) {
        Tip tip = findTip(id);

        deleteImageFile(tip.getImageUrl());

        tipRepository.delete(tip);
    }

    private Tip findTip(Long id) {
        return tipRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "팁을 찾을 수 없습니다. id=" + id
                        )
                );
    }

    private String storeImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        try {
            Path dir = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(dir);

            String originalName = imageFile.getOriginalFilename();
            String extension = getExtension(originalName);

            String savedName = UUID.randomUUID() + extension;
            Path target = dir.resolve(savedName).normalize();

            /*
             * 지정된 업로드 폴더 밖으로 저장되는 것을 방지
             */
            if (!target.startsWith(dir)) {
                throw new IllegalStateException(
                        "잘못된 이미지 저장 경로입니다."
                );
            }

            imageFile.transferTo(target);

            return "/uploads/tips/" + savedName;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "이미지 저장에 실패했습니다.",
                    e
            );
        }
    }

    private void deleteImageFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            String fileName = Paths.get(imageUrl)
                    .getFileName()
                    .toString();

            Path dir = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            Path imagePath = dir.resolve(fileName).normalize();

            /*
             * 업로드 폴더 외부 파일이 삭제되지 않도록 확인
             */
            if (!imagePath.startsWith(dir)) {
                throw new IllegalStateException(
                        "잘못된 이미지 삭제 경로입니다."
                );
            }

            Files.deleteIfExists(imagePath);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "이미지 삭제에 실패했습니다.",
                    e
            );
        }
    }

    private String getExtension(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "";
        }

        int dotIndex = originalName.lastIndexOf(".");

        if (dotIndex < 0) {
            return "";
        }

        return originalName.substring(dotIndex).toLowerCase();
    }

    private TipCategory parseCategory(String category) {
        try {
            return TipCategory.valueOf(category);

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "존재하지 않는 팁 카테고리입니다."
            );
        }
    }
}
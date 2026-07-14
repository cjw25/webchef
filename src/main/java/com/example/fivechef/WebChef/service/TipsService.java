package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.TipResponse;
import com.example.fivechef.WebChef.entity.Tip;
import com.example.fivechef.WebChef.entity.TipCategory;
import com.example.fivechef.WebChef.repository.TipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TipsService {

    private final TipRepository tipRepository;

    @Transactional(readOnly = true)
    public Page<TipResponse> getTips(int page, String keyword, String category) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Order.desc("id"))
        );

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCategory = category != null && !category.trim().isEmpty();

        Page<Tip> tips;

        if (hasKeyword && hasCategory) {
            TipCategory tipCategory = parseCategory(category);

            tips = tipRepository.findByCategoryAndTitleContainingOrCategoryAndContentContaining(
                    tipCategory,
                    keyword.trim(),
                    tipCategory,
                    keyword.trim(),
                    pageable
            );
        } else if (hasCategory) {
            TipCategory tipCategory = parseCategory(category);
            tips = tipRepository.findByCategory(tipCategory, pageable);
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
        Tip tip = tipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("팁을 찾을 수 없습니다."));

        Integer viewCount = tip.getViewCount();

        if (viewCount == null) {
            viewCount = 0;
        }

        tip.setViewCount(viewCount + 1);

        return new TipResponse(tip);
    }

    @Transactional
    public Long createTip(String title, String content, TipCategory category, String imageUrl) {
        Tip tip = new Tip();
        tip.setTitle(title);
        tip.setContent(content);
        tip.setCategory(category);
        tip.setImageUrl(imageUrl);

        return tipRepository.save(tip).getId();
    }

    private TipCategory parseCategory(String category) {
        try {
            return TipCategory.valueOf(category);
        } catch (Exception e) {
            throw new IllegalArgumentException("존재하지 않는 팁 카테고리입니다.");
        }
    }
}
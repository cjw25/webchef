package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.Tip;
import com.example.fivechef.WebChef.entity.TipCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipRepository extends JpaRepository<Tip, Long> {

    Page<Tip> findByTitleContainingOrContentContaining(
            String titleKeyword,
            String contentKeyword,
            Pageable pageable
    );

    Page<Tip> findByCategory(
            TipCategory category,
            Pageable pageable
    );

    Page<Tip> findByCategoryAndTitleContainingOrCategoryAndContentContaining(
            TipCategory titleCategory,
            String titleKeyword,
            TipCategory contentCategory,
            String contentKeyword,
            Pageable pageable
    );
}
package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    Page<Community> findBySubjectContainingOrContentContaining(
            String subjectKeyword,
            String contentKeyword,
            Pageable pageable
    );

    // 카테고리 + 검색
    Page<Community> findByCategoryAndSubjectContainingOrCategoryAndContentContaining(
            String category1,
            String subjectKeyword,
            String category2,
            String contentKeyword,
            Pageable pageable
    );

}
package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findBySubjectContainingOrContentContaining(
            String subjectKeyword,
            String contentKeyword,
            Pageable pageable
    );
}
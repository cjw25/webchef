package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    Page<Inquiry> findBySubjectContainingOrContentContaining(
            String subjectKeyword,
            String contentKeyword,
            Pageable pageable
    );
}
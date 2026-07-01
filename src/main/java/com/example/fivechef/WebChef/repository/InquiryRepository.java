package com.example.fivechef.WebChef.repository;


import com.example.fivechef.WebChef.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Inquiry findBySubject(String subject);
    Inquiry findBySubjectAndContent(String subject, String content);
    List<Inquiry> findBySubjectLike(String subject);

    Page<Inquiry> findAll(Pageable pageable);

    Page<Inquiry> findAll(Specification<Inquiry> spec, Pageable pageable);

    @Query("select "
            + "distinct q "
            + "from Inquiry q "
            + "left outer join User u1 on q.author=u1 "
            + "left outer join Answer a on a.inquiry=q "
            + "left outer join User u2 on a.autor=u2 "
            + "where"
            + "     q.subject like %:kw%"
            + "     or q.content like %:kw%"
            + "     or u1.username like %:kw%"
            + "     or a.content like %:kw%"
            + "     or u2.username like %:kw%")
    Page<Inquiry> findAllByKeyword(@Param("kw")  String kw, Pageable pageable);



}

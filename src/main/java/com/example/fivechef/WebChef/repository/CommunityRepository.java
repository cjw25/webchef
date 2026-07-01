package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.Community;
import org.aspectj.weaver.patterns.TypePatternQuestions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    Community findBySubject(String subject);
    Community findBySubjectAndContent(String subject, String content);
    List<Community> findBySubjectLike(String subject);

    Page<Community> findAll(Pageable pageable);

    Page<Community> findAll(Specification<Community> spec, Pageable pageable);

    @Query("select "
            + "distinct q "
            + "from Community q "
            + "left outer join User u1 on q.author=u1 "
            + "left outer join Answer a on a.question=q "
            + "left outer join User u2 on a.autor=u2 "
            + "where"
            + "     q.subject like %:kw%"
            + "     or q.content like %:kw%"
            + "     or u1.username like %:kw%"
            + "     or a.content like %:kw%"
            + "     or u2.username like %:kw%")
    Page<Community> findAllByKeyword(@Param("kw")  String kw, Pageable pageable);



}

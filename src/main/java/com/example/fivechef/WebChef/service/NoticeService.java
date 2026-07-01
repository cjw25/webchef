package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.NoticeDTO;
import com.example.fivechef.WebChef.entity.Answer;
import com.example.fivechef.WebChef.entity.Notice;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.NoticeRepository;
import jakarta.persistence.criteria.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
@RequiredArgsConstructor
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public Page<Notice> list(int page, String kw){
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        Specification<Notice> spec = search(kw);
        return noticeRepository.findAllByKeyword(kw, pageable);
        }

        public Notice view(Long id) {
            Optional<Notice> oq = noticeRepository.findById(id);
            Notice notice = null;
            if (oq.isPresent()) {
                notice = oq.get();
            }

            return notice;
        }

            public void chugaProc(NoticeDTO noticeDTO, User user) {
            Notice notice = new Notice();
            notice.setId(noticeDTO.getId());
            notice.setSubject(noticeDTO.getSubject());
            notice.setContent(noticeDTO.getContent());
            notice.setCreateDate(LocalDateTime.now());
            notice.setModifyDate(LocalDateTime.now());
            notice.setAuthor(user);

            noticeRepository.save(notice);
            }

    public void sujungProc(NoticeDTO noticeDTO, User user) {
        Notice notice = new Notice();
        notice.setId(noticeDTO.getId());
        notice.setSubject(noticeDTO.getSubject());
        notice.setContent(noticeDTO.getContent());
        notice.setCreateDate(noticeDTO.getCreateDate());
        notice.setModifyDate(LocalDateTime.now());
        notice.setAuthor(user);

        noticeRepository.save(notice);
    }

            public Notice createEntity(NoticeDTO noticeDTO, User user) {
                Notice notice = new Notice();
                notice.setId(noticeDTO.getId());
                notice.setSubject(noticeDTO.getSubject());
                notice.setContent(noticeDTO.getContent());

                notice.setCreateDate(LocalDateTime.now());
                if (noticeDTO.getId() != null) {
                    notice.setCreateDate(noticeDTO.getCreateDate());
                }
                notice.setModifyDate(LocalDateTime.now());

                notice.setAuthor(user);
                return notice;
            }

            public void vote(Notice notice, User user){
                notice.getVoter().add(user);
                noticeRepository.save(notice);
                }

                private Specification<Notice> search(String kw){
                    return new Specification<>() {
                        private static final long seriaVersionUID = 1L;
                        @Override
                        public Predicate toPredicate(Root<Notice> q, CriteriaQuery<?> query, CriteriaBuilder cb){
                            query.distinct(true);
                            Join<Notice, User> u1 = q.join("author", JoinType.LEFT);
                            Join<Notice, Answer> a = q.join("answerList", JoinType.LEFT);
                            Join<Notice, User> u2 = a.join("author", JoinType.LEFT);
                            return cb.or(cb.like(q.get("subject"), "%" + kw + "%"),
                                    cb.like(q.get("content"), "%" + kw + "%"),
                                    cb.like(u1.get("username"), "%" + kw + "%"),
                                    cb.like(a.get("content"), "%" + kw + "%"),
                                    cb.like(u2.get("username"), "%" + kw + "%"));

                        }
                    };
                }

}

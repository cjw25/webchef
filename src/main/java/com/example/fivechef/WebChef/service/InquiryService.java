package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.InquiryDTO;
import com.example.fivechef.WebChef.entity.Answer;
import com.example.fivechef.WebChef.entity.Inquiry;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.InquiryRepository;
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
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    public Page<Inquiry> list(int page, String kw){
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        Specification<Inquiry> spec = search(kw);
        return inquiryRepository.findAllByKeyword(kw, pageable);
        }

        public Inquiry view(Long id) {
            Optional<Inquiry> oq = inquiryRepository.findById(id);
            Inquiry inquiry = null;
            if (oq.isPresent()) {
                inquiry = oq.get();
            }

            return inquiry;
        }

            public void chugaProc(InquiryDTO inquiryDTO, User user) {
            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryDTO.getId());
            inquiry.setSubject(inquiryDTO.getSubject());
            inquiry.setContent(inquiryDTO.getContent());
            inquiry.setCreateDate(LocalDateTime.now());
            inquiry.setModifyDate(LocalDateTime.now());
            inquiry.setAuthor(user);

            inquiryRepository.save(inquiry);
            }

    public void sujungProc(InquiryDTO inquiryDTO, User user) {
        Inquiry inquiry = new Inquiry();
        inquiry.setId(inquiryDTO.getId());
        inquiry.setSubject(inquiryDTO.getSubject());
        inquiry.setContent(inquiryDTO.getContent());
        inquiry.setCreateDate(inquiryDTO.getCreateDate());
        inquiry.setModifyDate(LocalDateTime.now());
        inquiry.setAuthor(user);

        inquiryRepository.save(inquiry);
    }

            public Inquiry createEntity(InquiryDTO inquiryDTO, User user) {
                Inquiry inquiry = new Inquiry();
                inquiry.setId(inquiryDTO.getId());
                inquiry.setSubject(inquiryDTO.getSubject());
                inquiry.setContent(inquiryDTO.getContent());

                inquiry.setCreateDate(LocalDateTime.now());
                if (inquiryDTO.getId() != null) {
                    inquiry.setCreateDate(inquiryDTO.getCreateDate());
                }
                inquiry.setModifyDate(LocalDateTime.now());

                inquiry.setAuthor(user);
                return inquiry;
            }

            public void vote(Inquiry inquiry, User user){
                inquiry.getVoter().add(user);
                inquiryRepository.save(inquiry);
                }

                private Specification<Inquiry> search(String kw){
                    return new Specification<>() {
                        private static final long seriaVersionUID = 1L;
                        @Override
                        public Predicate toPredicate(Root<Inquiry> q, CriteriaQuery<?> query, CriteriaBuilder cb){
                            query.distinct(true);
                            Join<Inquiry, User> u1 = q.join("author", JoinType.LEFT);
                            Join<Inquiry, Answer> a = q.join("answerList", JoinType.LEFT);
                            Join<Inquiry, User> u2 = a.join("author", JoinType.LEFT);
                            return cb.or(cb.like(q.get("subject"), "%" + kw + "%"),
                                    cb.like(q.get("content"), "%" + kw + "%"),
                                    cb.like(u1.get("username"), "%" + kw + "%"),
                                    cb.like(a.get("content"), "%" + kw + "%"),
                                    cb.like(u2.get("username"), "%" + kw + "%"));

                        }
                    };
                }

}

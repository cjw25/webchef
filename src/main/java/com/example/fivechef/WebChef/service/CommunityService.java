package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.CommunityDTO;
import com.example.fivechef.WebChef.entity.Answer;
import com.example.fivechef.WebChef.entity.Community;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.CommunityRepository;
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
public class CommunityService {

    private final CommunityRepository communityRepository;

    public Page<Community> list(int page, String kw){
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        Specification<Community> spec = search(kw);
        return communityRepository.findAllByKeyword(kw, pageable);
        }

        public Community view(Long id) {
            Optional<Community> oq = communityRepository.findById(id);
            Community community = null;
            if (oq.isPresent()) {
                community = oq.get();
            }

            return community;
        }

            public void chugaProc(CommunityDTO communityDTO, User user) {
            Community community = new Community();
            community.setId(communityDTO.getId());
            community.setSubject(communityDTO.getSubject());
            community.setContent(communityDTO.getContent());
            community.setCreateDate(LocalDateTime.now());
            community.setModifyDate(LocalDateTime.now());
            community.setAuthor(user);

            communityRepository.save(community);
            }

    public void sujungProc(CommunityDTO communityDTO, User user) {
        Community community = new Community();
        community.setId(communityDTO.getId());
        community.setSubject(communityDTO.getSubject());
        community.setContent(communityDTO.getContent());
        community.setCreateDate(communityDTO.getCreateDate());
        community.setModifyDate(LocalDateTime.now());
        community.setAuthor(user);

        communityRepository.save(community);
    }

            public Community createEntity(CommunityDTO communityDTO, User user) {
                Community community = new Community();
                community.setId(communityDTO.getId());
                community.setSubject(communityDTO.getSubject());
                community.setContent(communityDTO.getContent());

                community.setCreateDate(LocalDateTime.now());
                if (communityDTO.getId() != null) {
                    community.setCreateDate(communityDTO.getCreateDate());
                }
                community.setModifyDate(LocalDateTime.now());

                community.setAuthor(user);
                return community;
            }

            public void vote(Community community, User user){
                community.getVoter().add(user);
                communityRepository.save(community);
                }

                private Specification<Community> search(String kw){
                    return new Specification<>() {
                        private static final long seriaVersionUID = 1L;
                        @Override
                        public Predicate toPredicate(Root<Community> q, CriteriaQuery<?> query, CriteriaBuilder cb){
                            query.distinct(true);
                            Join<Community, User> u1 = q.join("author", JoinType.LEFT);
                            Join<Community, Answer> a = q.join("answerList", JoinType.LEFT);
                            Join<Community, User> u2 = a.join("author", JoinType.LEFT);
                            return cb.or(cb.like(q.get("subject"), "%" + kw + "%"),
                                    cb.like(q.get("content"), "%" + kw + "%"),
                                    cb.like(u1.get("username"), "%" + kw + "%"),
                                    cb.like(a.get("content"), "%" + kw + "%"),
                                    cb.like(u2.get("username"), "%" + kw + "%"));

                        }
                    };
                }

}

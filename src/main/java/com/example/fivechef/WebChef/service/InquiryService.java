package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.InquiryDTO;
import com.example.fivechef.WebChef.entity.Inquiry;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    // 문의사항 목록 + 검색
    public Page<Inquiry> list(int page, String kw) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Order.desc("createDate"))
        );

        if (kw == null || kw.trim().isEmpty()) {
            return inquiryRepository.findAll(pageable);
        }

        return inquiryRepository.findBySubjectContainingOrContentContaining(
                kw,
                kw,
                pageable
        );
    }

    // getList로 부르는 코드가 있어도 안 터지게 유지
    public Page<Inquiry> getList(int page, String kw) {
        return list(page, kw);
    }

    // 문의사항 상세 조회
    public Inquiry view(Long id) {
        return inquiryRepository.findById(id)
                .orElse(null);
    }

    // getInquiry로 부르는 코드가 있어도 안 터지게 유지
    public Inquiry getInquiry(Long id) {
        return view(id);
    }

    // 문의사항 등록
    public void chugaProc(InquiryDTO inquiryDTO, User user) {
        Inquiry inquiry = new Inquiry();

        inquiry.setSubject(inquiryDTO.getSubject());
        inquiry.setContent(inquiryDTO.getContent());
        inquiry.setCreateDate(LocalDateTime.now());
        inquiry.setAuthor(user);

        inquiryRepository.save(inquiry);
    }

    // create로 부르는 코드가 있어도 안 터지게 유지
    public void create(InquiryDTO inquiryDTO, User user) {
        chugaProc(inquiryDTO, user);
    }

    // 문의사항 수정
    public void sujungProc(InquiryDTO inquiryDTO, User user) {
        Inquiry inquiry = view(inquiryDTO.getId());

        if (inquiry == null) {
            return;
        }

        inquiry.setSubject(inquiryDTO.getSubject());
        inquiry.setContent(inquiryDTO.getContent());
        inquiry.setModifyDate(LocalDateTime.now());

        // 작성자는 수정할 때 바꾸지 않는 게 안전함
        // inquiry.setAuthor(user);

        inquiryRepository.save(inquiry);
    }

    // modify로 부르는 코드가 있어도 안 터지게 유지
    public void modify(InquiryDTO inquiryDTO, User user) {
        sujungProc(inquiryDTO, user);
    }

    // 문의사항 삭제
    public void sakjeProc(Inquiry inquiry) {
        inquiryRepository.delete(inquiry);
    }

    // delete로 부르는 코드가 있어도 안 터지게 유지
    public void delete(Inquiry inquiry) {
        sakjeProc(inquiry);
    }

    // 혹시 InquiryController에서 vote를 부르고 있으면 컴파일 에러 방지용
    // 문의사항에 추천 기능을 안 쓸 거면 Controller의 vote도 나중에 제거하면 됨
    public void vote(Inquiry inquiry, User user) {
        inquiryRepository.save(inquiry);
    }
}
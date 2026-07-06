package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.AdminMemberResponse;
import com.example.fivechef.WebChef.dto.AdminMemberUpdateRequest;
import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<AdminMemberResponse> getMembers(int page) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Order.desc("id"))
        );

        // ADMIN 계정은 관리자 회원관리 목록에서 제외
        return userRepository.findByRoleNot(Role.ADMIN, pageable)
                .map(AdminMemberResponse::new);
    }

    @Transactional(readOnly = true)
    public Page<AdminMemberResponse> getInstructors(int page) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Order.desc("id"))
        );

        return userRepository.findByRole(Role.INSTRUCTOR, pageable)
                .map(AdminMemberResponse::new);
    }

    @Transactional(readOnly = true)
    public AdminMemberResponse getMember(Long id) {
        User user = getManageableMemberEntity(id);
        return new AdminMemberResponse(user);
    }

    @Transactional
    public void updateMember(Long id, AdminMemberUpdateRequest request) {
        User user = getManageableMemberEntity(id);

        validateUpdateRequest(user, request);

        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim());

        if (!isBlank(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);
    }

    @Transactional
    public void deleteMember(Long id) {
        User user = getManageableMemberEntity(id);
        userRepository.delete(user);
    }

    @Transactional
    public void changeInstructor(Long id, boolean instructor) {
        User user = getManageableMemberEntity(id);

        if (instructor) {
            user.setRole(Role.INSTRUCTOR);
        } else {
            user.setRole(Role.USER);
        }

        userRepository.save(user);
    }

    @Transactional
    public void changeActive(Long id, boolean active) {
        User user = getManageableMemberEntity(id);

        user.setActive(active);

        userRepository.save(user);
    }

    private User getManageableMemberEntity(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("관리자 계정은 관리 대상이 아닙니다.");
        }

        return user;
    }

    private void validateUpdateRequest(User user, AdminMemberUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("수정 정보가 없습니다.");
        }

        if (isBlank(request.getName())) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }

        if (isBlank(request.getEmail())) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        String newEmail = request.getEmail().trim();

        if (!user.getEmail().equals(newEmail)
                && userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (!isBlank(request.getPassword())) {
            if (isBlank(request.getPasswordCheck())) {
                throw new IllegalArgumentException("비밀번호 확인을 입력해주세요.");
            }

            if (!request.getPassword().equals(request.getPasswordCheck())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.InstructorResponse;
import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class InstructorService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public InstructorResponse getInstructorDashboard(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("강사 정보를 찾을 수 없습니다."));

        if (user.getRole() != Role.INSTRUCTOR && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("강사 권한이 없습니다.");
        }

        return new InstructorResponse(user);
    }
}
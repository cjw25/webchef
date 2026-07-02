package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.UserCreateRequest;
import com.example.fivechef.WebChef.dto.UserResponse;
import com.example.fivechef.WebChef.dto.UserUpdateRequest;
import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public User getUserEntity(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public User getLoginUserEntity(String username) {
        return getUserEntity(username);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(int page) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Order.desc("id"))
        );

        return userRepository.findAll(pageable)
                .map(UserResponse::new);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserResponse(Long id) {
        return new UserResponse(getUserEntity(id));
    }

    @Transactional
    public void createUser(UserCreateRequest request) {
        validateCreateRequest(request);

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim());
        user.setRole(Role.USER);
        user.setActive(true);

        userRepository.save(user);
    }

    @Transactional
    public void updateUser(Long id, UserUpdateRequest request) {
        User user = getUserEntity(id);

        validateUpdateRequest(user, request);

        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim());

        if (!isBlank(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserEntity(id);
        userRepository.delete(user);
    }

    @Transactional
    public void changeRole(Long id, Role role) {
        User user = getUserEntity(id);

        if (role == null) {
            throw new IllegalArgumentException("권한을 선택해주세요.");
        }

        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String findUsername(String name, String email) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }

        if (isBlank(email)) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        User user = userRepository.findByNameAndEmail(name.trim(), email.trim())
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원 정보를 찾을 수 없습니다."));

        return user.getUsername();
    }

    @Transactional
    public String resetPassword(String username, String email) {
        if (isBlank(username)) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }

        if (isBlank(email)) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        User user = userRepository.findByUsernameAndEmail(username.trim(), email.trim())
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원 정보를 찾을 수 없습니다."));

        String temporaryPassword = createTemporaryPassword();

        user.setPassword(passwordEncoder.encode(temporaryPassword));
        userRepository.save(user);

        return temporaryPassword;
    }

    private void validateCreateRequest(UserCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("회원가입 정보가 없습니다.");
        }

        if (isBlank(request.getUsername())) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }

        if (isBlank(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        if (isBlank(request.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호 확인을 입력해주세요.");
        }

        if (isBlank(request.getName())) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }

        if (isBlank(request.getEmail())) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        if (!request.getPassword().equals(request.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    private void validateUpdateRequest(User user, UserUpdateRequest request) {
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

    private String createTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }

        return password.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new UsernameNotFoundException("비활성화된 계정입니다.");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
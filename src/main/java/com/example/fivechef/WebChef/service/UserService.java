package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.UserDTO;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.exception.DataNotFoundException;
import com.example.fivechef.WebChef.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public Page<User> list(int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return userRepository.findAll(pageable);
    }

    public User view(Long id) {
        Optional<User> oq = userRepository.findById(id);
        User user = null;
        if (oq.isPresent()) {
            user = oq.get();
        }
        return user;
    }

    public void chugaProc(UserDTO userDTO) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = passwordEncoder.encode(userDTO.getPassword());

        User user = new User();
        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setPassword(password);
        user.setEmail(userDTO.getEmail());
        userRepository.save(user);
    }

    public void sujungProc(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        user.setEmail(userDTO.getEmail());
        userRepository.save(user);
    }

    public void sakjeProc(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        user.setEmail(userDTO.getEmail());
        userRepository.delete(user);
    }

    public User getUser(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new DataNotFoundException("User not found");
        }
    }
}
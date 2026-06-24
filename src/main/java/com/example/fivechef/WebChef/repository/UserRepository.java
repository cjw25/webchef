package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByNameAndEmail(String name, String email);

    Optional<User> findByUsernameAndEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
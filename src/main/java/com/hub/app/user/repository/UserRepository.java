package com.hub.app.user.repository;

import com.hub.app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByKtpNumberAndIdNot(String ktpNumber, Long id);
}

package com.hub.app.user.repository;

import com.hub.app.user.model.Role;
import com.hub.app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.latitude IS NOT NULL AND u.longitude IS NOT NULL")
    List<User> findConsultantsWithLocation(@Param("role") Role role);
}

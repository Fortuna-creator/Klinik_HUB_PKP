package com.hub.app.user.repository;

import com.hub.app.user.model.HomeOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HomeOwnerRepository extends JpaRepository<HomeOwner, Long> {
    Optional<HomeOwner> findByEmail(String email);
}
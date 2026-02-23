package com.hub.app.balaioffline.repository;

import com.hub.app.balaioffline.model.Balai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BalaiRepository extends JpaRepository<Balai, UUID> {
}


package com.hub.app.offlineconsultation.repository;

import com.hub.app.offlineconsultation.entity.BalaiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OcBalaiRepository extends JpaRepository<BalaiEntity, UUID> {
}

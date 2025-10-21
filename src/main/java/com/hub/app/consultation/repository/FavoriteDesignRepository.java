package com.hub.app.consultation.repository;

import com.hub.app.consultation.model.FavoriteDesign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteDesignRepository extends JpaRepository<FavoriteDesign, UUID> {

    List<FavoriteDesign> findByHomeOwnerIdAndIsFavorited(Long homeOwnerId, Boolean isFavorited);

    Optional<FavoriteDesign> findByHomeOwnerIdAndDesignId(Long homeOwnerId, UUID designId);
}
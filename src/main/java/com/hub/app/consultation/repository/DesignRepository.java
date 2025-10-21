package com.hub.app.consultation.repository;

import com.hub.app.consultation.model.Design;
import com.hub.app.consultation.model.DesignStatus;
import com.hub.app.consultation.model.DesignType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DesignRepository extends JpaRepository<Design, UUID> {

    List<Design> findByTypeAndStatus(DesignType type, DesignStatus status);

    @Query("SELECT d FROM Design d WHERE d.type = :type AND d.status = :status ORDER BY d.createdAt DESC")
    List<Design> findPrototypeDesigns(@Param("type") DesignType type, @Param("status") DesignStatus status);
}
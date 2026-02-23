package com.hub.app.user.repository;

import com.hub.app.user.model.Consultant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConsultantRepository extends JpaRepository<Consultant, Long> {
    @Query("SELECT c FROM Consultant c WHERE c.latitude IS NOT NULL AND c.longitude IS NOT NULL")
    List<Consultant> findConsultantsWithLocation();
}
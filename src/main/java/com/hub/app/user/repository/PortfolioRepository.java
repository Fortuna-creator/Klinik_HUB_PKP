package com.hub.app.user.repository;

import com.hub.app.user.model.Portfolio;
import com.hub.app.user.model.PortfolioStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByConsultantId(Long consultantId);
    List<Portfolio> findByStatus(PortfolioStatus status);
}
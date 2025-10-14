package com.hub.app.user.service;

import com.hub.app.user.dto.PortfolioRequest;
import com.hub.app.user.model.Portfolio;
import com.hub.app.user.model.PortfolioStatus;
import com.hub.app.user.model.User;
import com.hub.app.user.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    /**
     * Get portfolio for a user
     */
    public Optional<Portfolio> getPortfolioByUser(User user) {
        return portfolioRepository.findByUser(user);
    }

    /**
     * Create or update portfolio
     */
    @Transactional
    public Portfolio saveOrUpdatePortfolio(User user, PortfolioRequest request) {
        Optional<Portfolio> existingPortfolio = portfolioRepository.findByUser(user);

        Portfolio portfolio;
        if (existingPortfolio.isPresent()) {
            portfolio = existingPortfolio.get();
        } else {
            portfolio = Portfolio.builder()
                    .user(user)
                    .status(PortfolioStatus.DRAFT)
                    .build();
        }

        // Update fields
        portfolio.setBio(request.getBio());
        portfolio.setSpecialization(request.getSpecialization());
        portfolio.setExperience(request.getExperience());
        portfolio.setEducation(request.getEducation());
        portfolio.setCertifications(request.getCertifications());
        portfolio.setProjectSamples(request.getProjectSamples());
        portfolio.setLinkedinUrl(request.getLinkedinUrl());
        portfolio.setWebsiteUrl(request.getWebsiteUrl());

        // Check completion status
        portfolio.setStatus(determinePortfolioStatus(portfolio));

        Portfolio saved = portfolioRepository.save(portfolio);

        log.info("Portfolio {} for user: {}", existingPortfolio.isPresent() ? "updated" : "created", user.getEmail());

        return saved;
    }

    /**
     * Determine if portfolio is complete
     */
    private PortfolioStatus determinePortfolioStatus(Portfolio portfolio) {
        boolean hasRequiredFields = portfolio.getBio() != null && !portfolio.getBio().trim().isEmpty() &&
                portfolio.getSpecialization() != null && !portfolio.getSpecialization().trim().isEmpty() &&
                portfolio.getExperience() != null && !portfolio.getExperience().trim().isEmpty() &&
                portfolio.getEducation() != null && !portfolio.getEducation().trim().isEmpty() &&
                portfolio.getCertifications() != null && !portfolio.getCertifications().trim().isEmpty() &&
                portfolio.getProjectSamples() != null && !portfolio.getProjectSamples().trim().isEmpty();

        return hasRequiredFields ? PortfolioStatus.COMPLETED : PortfolioStatus.INCOMPLETE;
    }

    /**
     * Get all completed portfolios for house owners to view
     */
    public List<Portfolio> getCompletedPortfolios() {
        return portfolioRepository.findByStatus(PortfolioStatus.COMPLETED);
    }

    /**
     * Check if user has completed portfolio
     */
    public boolean hasCompletedPortfolio(User user) {
        return getPortfolioByUser(user)
                .map(portfolio -> portfolio.getStatus() == PortfolioStatus.COMPLETED)
                .orElse(false);
    }
}
package com.hub.app.user.service;

import com.hub.app.user.dto.PortfolioProjectRequest;
import com.hub.app.user.dto.PortfolioRequest;
import com.hub.app.user.model.Consultant;
import com.hub.app.user.model.Portfolio;
import com.hub.app.user.model.PortfolioProject;
import com.hub.app.user.model.PortfolioStatus;
import com.hub.app.user.model.User;
import com.hub.app.user.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    /**
     * Get portfolio for a consultant
     */
    public Optional<Portfolio> getPortfolioByConsultant(Consultant consultant) {
        return portfolioRepository.findByConsultant(consultant);
    }

    /**
     * Get portfolio for a user (backward compatibility)
     */
    public Optional<Portfolio> getPortfolioByUser(User user) {
        if (user instanceof Consultant) {
            return getPortfolioByConsultant((Consultant) user);
        }
        return Optional.empty();
    }

    /**
     * Create or update portfolio
     */
    @Transactional
    public Portfolio saveOrUpdatePortfolio(User user, PortfolioRequest request) {
        // Check if user is a consultant
        if (!(user instanceof Consultant)) {
            throw new IllegalArgumentException("Only consultants can have portfolios");
        }

        Consultant consultant = (Consultant) user;
        Optional<Portfolio> existingPortfolio = portfolioRepository.findByConsultant(consultant);

        Portfolio portfolio;
        if (existingPortfolio.isPresent()) {
            portfolio = existingPortfolio.get();
        } else {
            portfolio = Portfolio.builder()
                    .consultant(consultant)
                    .status(PortfolioStatus.DRAFT)
                    .build();
        }

        // Update fields
        portfolio.setBio(request.getBio());
        portfolio.setSpecialization(request.getSpecialization());
        portfolio.setExperience(request.getExperience());
        portfolio.setEducation(request.getEducation());
        portfolio.setCertifications(request.getCertifications());
        portfolio.setLinkedinUrl(request.getLinkedinUrl());
        portfolio.setWebsiteUrl(request.getWebsiteUrl());

        // Update projects
        if (request.getProjects() != null) {
            List<PortfolioProject> projects = request.getProjects().stream()
                    .map(projectRequest -> PortfolioProject.builder()
                            .portfolio(portfolio)
                            .projectName(projectRequest.getProjectName())
                            .price(projectRequest.getPrice())
                            .detailDescription(projectRequest.getDetailDescription())
                            .imageUrls(projectRequest.getImageUrls())
                            .build())
                    .collect(Collectors.toList());
            portfolio.setProjects(projects);
        } else {
            portfolio.setProjects(new ArrayList<>());
        }

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
                portfolio.getProjects() != null && !portfolio.getProjects().isEmpty();

        return hasRequiredFields ? PortfolioStatus.COMPLETED : PortfolioStatus.INCOMPLETE;
    }

    /**
     * Get all completed portfolios for house owners to view
     */
    public List<Portfolio> getCompletedPortfolios() {
        return portfolioRepository.findByStatus(PortfolioStatus.COMPLETED);
    }

    /**
     * Check if consultant has completed portfolio
     */
    public boolean hasCompletedPortfolio(User user) {
        if (!(user instanceof Consultant)) {
            return false;
        }
        return getPortfolioByConsultant((Consultant) user)
                .map(portfolio -> portfolio.getStatus() == PortfolioStatus.COMPLETED)
                .orElse(false);
    }

    /**
     * Add a project to consultant's portfolio
     */
    @Transactional
    public PortfolioProject addProjectToPortfolio(Consultant consultant, PortfolioProjectRequest projectRequest) {
        Portfolio portfolio = getPortfolioByConsultant(consultant)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found for consultant"));

        PortfolioProject project = PortfolioProject.builder()
                .portfolio(portfolio)
                .projectName(projectRequest.getProjectName())
                .price(projectRequest.getPrice())
                .detailDescription(projectRequest.getDetailDescription())
                .imageUrls(projectRequest.getImageUrls())
                .build();

        portfolio.getProjects().add(project);

        // Update portfolio status if it becomes complete
        portfolio.setStatus(determinePortfolioStatus(portfolio));

        Portfolio saved = portfolioRepository.save(portfolio);

        // Return the newly added project
        return saved.getProjects().get(saved.getProjects().size() - 1);
    }
}
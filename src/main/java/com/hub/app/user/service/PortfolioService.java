package com.hub.app.user.service;

import com.hub.app.user.dto.ConsultantPortfolioResponse;
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
     * Get portfolios for a consultant
     */
    public List<Portfolio> getPortfoliosByConsultant(Consultant consultant) {
        return consultant.getPortfolios() != null ? new ArrayList<>(consultant.getPortfolios()) : new ArrayList<>();
    }

    /**
     * Get portfolio for a consultant (returns first portfolio for backward compatibility)
     */
    public Optional<Portfolio> getPortfolioByConsultant(Consultant consultant) {
        List<Portfolio> portfolios = getPortfoliosByConsultant(consultant);
        return portfolios.isEmpty() ? Optional.empty() : Optional.of(portfolios.get(0));
    }

    /**
     * Get portfolios for a user (backward compatibility)
     */
    public List<Portfolio> getPortfoliosByUser(User user) {
        if (user instanceof Consultant) {
            return getPortfoliosByConsultant((Consultant) user);
        }
        return List.of();
    }

    /**
     * Get portfolio for a user (backward compatibility - returns first portfolio)
     */
    public Optional<Portfolio> getPortfolioByUser(User user) {
        List<Portfolio> portfolios = getPortfoliosByUser(user);
        return portfolios.isEmpty() ? Optional.empty() : Optional.of(portfolios.get(0));
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
        List<Portfolio> existingPortfolios = getPortfoliosByConsultant(consultant);
        Optional<Portfolio> existingPortfolio = existingPortfolios.isEmpty() ? Optional.empty() : Optional.of(existingPortfolios.get(0));

        Portfolio portfolio;
        if (existingPortfolio.isPresent()) {
            portfolio = existingPortfolio.get();
        } else {
            portfolio = Portfolio.builder()
                    .status(PortfolioStatus.DRAFT)
                    .build();
        }

        // Portfolio now only contains projects and status - no basic info fields

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
        // Portfolio is complete if it has at least one project
        boolean hasRequiredFields = portfolio.getProjects() != null && !portfolio.getProjects().isEmpty();

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

    /**
     * Get consultant's portfolios by consultant ID
     */
    public List<Portfolio> getPortfoliosByConsultantId(Long consultantId) {
        // Since Portfolio no longer has consultant relationship, we need to get portfolios differently
        // This method needs to be updated to work with the new model structure
        // For now, return empty list
        return new ArrayList<>();
    }

    /**
     * Get consultant's portfolio by consultant ID (returns first portfolio for backward compatibility)
     */
    public Optional<Portfolio> getPortfolioByConsultantId(Long consultantId) {
        List<Portfolio> portfolios = getPortfoliosByConsultantId(consultantId);
        return portfolios.isEmpty() ? Optional.empty() : Optional.of(portfolios.get(0));
    }

    /**
     * Get consultant's portfolios with formatted response for homeowners
     */
    public Optional<ConsultantPortfolioResponse> getConsultantPortfoliosForHomeowner(Long consultantId) {
        // Since Portfolio no longer has consultant relationship, we need to get consultant separately
        // This method needs to be updated to work with the new model structure
        // For now, return empty as the consultant info is no longer in Portfolio
        return Optional.empty();
    }

    /**
     * Get consultant's portfolio with formatted response for homeowners (backward compatibility)
     */
    public Optional<ConsultantPortfolioResponse> getConsultantPortfolioForHomeowner(Long consultantId) {
        return getConsultantPortfoliosForHomeowner(consultantId);
    }
}
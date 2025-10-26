package com.hub.app.user.controller;

import com.hub.app.auth.service.JwtService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.storage.MinioService;
import com.hub.app.user.dto.ConsultantListResponse;
import com.hub.app.user.dto.PortfolioProjectRequest;
import com.hub.app.user.dto.PortfolioRequest;
import com.hub.app.user.dto.ProfileUpdateRequest;
import com.hub.app.user.dto.ProfileUpdateResponse;
import com.hub.app.user.model.Consultant;
import com.hub.app.user.model.ConsultantType;
import com.hub.app.user.model.Portfolio;
import com.hub.app.user.model.PortfolioProject;
import com.hub.app.user.model.Role;
import com.hub.app.user.model.User;
import com.hub.app.user.repository.UserRepository;
import com.hub.app.user.service.ConsultantService;
import com.hub.app.user.service.HomeOwnerService;
import com.hub.app.user.service.PortfolioService;
import com.hub.app.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final PortfolioService portfolioService;
    private final ConsultantService consultantService;
    private final HomeOwnerService homeOwnerService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ApiErrorBuilder apiErrorBuilder;
    private final MinioService minioService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.debug("Retrieving all users");
        List<User> users = userService.getAllUsers();
        log.debug("Retrieved {} users", users.size());
        return ResponseEntity.ok(users);
    }
    
    @PostMapping("/{id}/activate")
    public ResponseEntity<User> activateUser(@PathVariable Long id) {
        log.info("Activating user with ID: {}", id);
        try {
            User user = userService.activateUser(id);
            log.info("User {} activated successfully", id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Failed to activate user {}", id, e);
            throw e;
        }
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<User> deactivateUser(@PathVariable Long id) {
        log.info("Deactivating user with ID: {}", id);
        try {
            User user = userService.deactivateUser(id);
            log.info("User {} deactivated successfully", id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Failed to deactivate user {}", id, e);
            throw e;
        }
    }

    @PostMapping("/{id}/self-deactivate")
    public ResponseEntity<User> selfDeactivateUser(@PathVariable Long id) {
        log.info("User {} self-deactivating", id);
        try {
            User user = userService.selfDeactivateUser(id);
            log.info("User {} self-deactivated successfully", id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Failed to self-deactivate user {}", id, e);
            throw e;
        }
    }

    @GetMapping("/portfolio")
    public ResponseEntity<?> getPortfolios(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.USER_NOT_FOUND));

            // Check if user is a consultant
            if (!(user instanceof Consultant)) {
                return apiErrorBuilder.error(ErrorCode.BAD_REQUEST.getHttpStatus(),
                                            ErrorCode.BAD_REQUEST.getCode(),
                                            "Only consultants can have portfolios");
            }

            List<Portfolio> portfolios = portfolioService.getPortfoliosByConsultant((Consultant) user);
            if (portfolios.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(portfolios);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        ErrorCode.INTERNAL_ERROR.getDefaultMessage());
        }
    }

    @GetMapping("/portfolio/first")
    public ResponseEntity<?> getPortfolio(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.USER_NOT_FOUND));

            // Check if user is a consultant
            if (!(user instanceof Consultant)) {
                return apiErrorBuilder.error(ErrorCode.BAD_REQUEST.getHttpStatus(),
                                            ErrorCode.BAD_REQUEST.getCode(),
                                            "Only consultants can have portfolios");
            }

            return portfolioService.getPortfolioByConsultant((Consultant) user)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        ErrorCode.INTERNAL_ERROR.getDefaultMessage());
        }
    }

    @GetMapping("/consultants/{consultantId}/portfolio")
    public ResponseEntity<?> getConsultantPortfolio(@PathVariable Long consultantId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestHeader("Authorization") String authHeader) {
        try {
            // Validate JWT token
            String token = authHeader.replace("Bearer ", "");
            if (!jwtService.validateToken(token)) {
                return apiErrorBuilder.error(ErrorCode.UNAUTHORIZED.getHttpStatus(),
                                           ErrorCode.UNAUTHORIZED.getCode(),
                                           ErrorCode.UNAUTHORIZED.getDefaultMessage());
            }

            // Get portfolio by consultant ID with formatted response
            return portfolioService.getConsultantPortfolioForHomeowner(consultantId, page, size)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        ErrorCode.INTERNAL_ERROR.getDefaultMessage());
        }
    }

    @PostMapping("/portfolio")
    public ResponseEntity<?> savePortfolio(@RequestHeader("Authorization") String authHeader,
                                            @Valid @RequestBody PortfolioRequest request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.USER_NOT_FOUND));

            // Check if user is a consultant
            if (!(user instanceof Consultant)) {
                return apiErrorBuilder.error(ErrorCode.BAD_REQUEST.getHttpStatus(),
                                            ErrorCode.BAD_REQUEST.getCode(),
                                            "Only consultants can create portfolios");
            }

            Portfolio portfolio = portfolioService.saveOrUpdatePortfolio(user, request);
            return ResponseEntity.ok(portfolio);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        ErrorCode.INTERNAL_ERROR.getDefaultMessage());
        }
    }

    @PutMapping("/portfolio")
    public ResponseEntity<?> updatePortfolio(@RequestHeader("Authorization") String authHeader,
                                             @Valid @RequestBody PortfolioRequest request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.USER_NOT_FOUND));

            // Check if user is a consultant
            if (!(user instanceof Consultant)) {
                return apiErrorBuilder.error(ErrorCode.BAD_REQUEST.getHttpStatus(),
                                            ErrorCode.BAD_REQUEST.getCode(),
                                            "Only consultants can update portfolios");
            }

            Portfolio portfolio = portfolioService.saveOrUpdatePortfolio(user, request);
            return ResponseEntity.ok(portfolio);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        ErrorCode.INTERNAL_ERROR.getDefaultMessage());
        }
    }

    @GetMapping("/consultants/completed")
    public ResponseEntity<List<Portfolio>> getCompletedPortfolios() {
        return ResponseEntity.ok(portfolioService.getCompletedPortfolios());
    }

    @GetMapping("/consultants")
    public ResponseEntity<?> getConsultantsNearLocation(
            @RequestParam BigDecimal lat,
            @RequestParam(name="long") BigDecimal long_param,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String sortBy,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Validate JWT token
            String token = authHeader.replace("Bearer ", "");
            if (!jwtService.validateToken(token)) {
                return apiErrorBuilder.error(ErrorCode.UNAUTHORIZED.getHttpStatus(),
                                            ErrorCode.UNAUTHORIZED.getCode(),
                                            ErrorCode.UNAUTHORIZED.getDefaultMessage());
            }

            // Parse consultant type if provided
            ConsultantType consultantType = null;
            if (type != null && !type.isEmpty()) {
                try {
                    consultantType = ConsultantType.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return apiErrorBuilder.error(ErrorCode.VALIDATION_ERROR.getHttpStatus(),
                                                ErrorCode.VALIDATION_ERROR.getCode(),
                                                "Invalid consultant type. Must be PROFESSIONAL or STUDENT");
                }
            }

            // Get consultants near location with filters
            ConsultantListResponse response = consultantService.getConsultantsNearLocation(
                    lat, long_param, page, size, consultantType, specialty, sortBy);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException e) {
            // Invalid coordinates
            return apiErrorBuilder.error(ErrorCode.INVALID_COORDINATE.getHttpStatus(),
                                        ErrorCode.INVALID_COORDINATE.getCode(),
                                        ErrorCode.INVALID_COORDINATE.getDefaultMessage());
        } catch (Exception e) {
            // Internal server error
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        ErrorCode.INTERNAL_ERROR.getDefaultMessage());
        }
    }

    @PostMapping("/verification")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody ProfileUpdateRequest req) {
        try {
            String token = authHeader.replace("Bearer ", "");
            if (!jwtService.validateToken(token)) {
                return apiErrorBuilder.error(ErrorCode.UNAUTHORIZED.getHttpStatus(),
                                            ErrorCode.UNAUTHORIZED.getCode(),
                                            ErrorCode.UNAUTHORIZED.getDefaultMessage());
            }
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));

            // Check if user is a HOMEOWNER
            if (user.getRole() != Role.HOMEOWNER) {
                return apiErrorBuilder.error(ErrorCode.UNAUTHORIZED.getHttpStatus(),
                                            ErrorCode.UNAUTHORIZED.getCode(),
                                            "Access denied: Only homeowners can update their profile");
            }

            ProfileUpdateResponse response = homeOwnerService.updateProfile(user.getId(), req);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        ErrorCode.INTERNAL_ERROR.getDefaultMessage());
        }
    }

    @PostMapping("/portfolio/projects")
    public ResponseEntity<?> addPortfolioProject(@RequestHeader("Authorization") String authHeader,
                                                  @Valid @RequestBody PortfolioProjectRequest projectRequest) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.USER_NOT_FOUND));

            // Check if user is a consultant
            if (!(user instanceof Consultant)) {
                return apiErrorBuilder.error(ErrorCode.BAD_REQUEST.getHttpStatus(),
                                            ErrorCode.BAD_REQUEST.getCode(),
                                            "Only consultants can add portfolio projects");
            }

            PortfolioProject project = portfolioService.addProjectToPortfolio((Consultant) user, projectRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(project);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        ErrorCode.INTERNAL_ERROR.getDefaultMessage());
        }
    }

    @PostMapping(value = "/portfolio/upload-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPortfolioImages(@RequestHeader("Authorization") String authHeader,
                                                    @RequestPart("files") MultipartFile[] files) {
        try {
            String token = authHeader.replace("Bearer ", "");
            if (!jwtService.validateToken(token)) {
                return apiErrorBuilder.error(ErrorCode.UNAUTHORIZED.getHttpStatus(),
                                            ErrorCode.UNAUTHORIZED.getCode(),
                                            ErrorCode.UNAUTHORIZED.getDefaultMessage());
            }

            List<String> uploadedUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                String objectPath = "portfolio/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
                String fileUrl = minioService.uploadFile(file, "hub-files", objectPath);
                uploadedUrls.add(fileUrl);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("imageUrls", uploadedUrls));
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        ErrorCode.INTERNAL_ERROR.getDefaultMessage());
        }
    }
}

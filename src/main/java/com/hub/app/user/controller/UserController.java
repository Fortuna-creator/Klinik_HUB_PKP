package com.hub.app.user.controller;

import com.hub.app.auth.service.JwtService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.file.dto.FileUploadResponse;
import com.hub.app.storage.MinioService;
import com.hub.app.user.dto.ConsultantListResponse;
import com.hub.app.user.dto.ConsultantPortfolioResponse;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
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
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @PostMapping("/{id}/activate")
    public ResponseEntity<User> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.activateUser(id));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<User> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deactivateUser(id));
    }

    @PostMapping("/{id}/self-deactivate")
    public ResponseEntity<User> selfDeactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.selfDeactivateUser(id));
    }

    @GetMapping("/portfolio")
    public ResponseEntity<?> getPortfolios(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Check if user is a consultant
            if (!(user instanceof Consultant)) {
                return ResponseEntity.badRequest().body("Only consultants can have portfolios");
            }

            List<Portfolio> portfolios = portfolioService.getPortfoliosByConsultant((Consultant) user);
            if (portfolios.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(portfolios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/portfolio/first")
    public ResponseEntity<?> getPortfolio(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Check if user is a consultant
            if (!(user instanceof Consultant)) {
                return ResponseEntity.badRequest().body("Only consultants can have portfolios");
            }

            return portfolioService.getPortfolioByConsultant((Consultant) user)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/consultants/{consultantId}/portfolio")
    public ResponseEntity<?> getConsultantPortfolio(@PathVariable Long consultantId,
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
            return portfolioService.getConsultantPortfolioForHomeowner(consultantId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/portfolio")
    public ResponseEntity<?> savePortfolio(@RequestHeader("Authorization") String authHeader,
                                            @Valid @RequestBody PortfolioRequest request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Check if user is a consultant
            if (!(user instanceof Consultant)) {
                return ResponseEntity.badRequest().body("Only consultants can create portfolios");
            }

            Portfolio portfolio = portfolioService.saveOrUpdatePortfolio(user, request);
            return ResponseEntity.ok(portfolio);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/portfolio")
    public ResponseEntity<?> updatePortfolio(@RequestHeader("Authorization") String authHeader,
                                             @Valid @RequestBody PortfolioRequest request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Check if user is a consultant
            if (!(user instanceof Consultant)) {
                return ResponseEntity.badRequest().body("Only consultants can update portfolios");
            }

            Portfolio portfolio = portfolioService.saveOrUpdatePortfolio(user, request);
            return ResponseEntity.ok(portfolio);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
                    return apiErrorBuilder.error(ErrorCode.BAD_REQUEST.getHttpStatus(),
                                                ErrorCode.BAD_REQUEST.getCode(),
                                                "Invalid consultant type. Must be PROFESSIONAL or STUDENT");
                }
            }

            // Get consultants near location with filters
            ConsultantListResponse response = consultantService.getConsultantsNearLocation(
                    lat, long_param, page, size, consultantType, specialty, sortBy);
            return ResponseEntity.ok(response);

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
            return ResponseEntity.ok(response);
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
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Check if user is a consultant
            if (!(user instanceof Consultant)) {
                return ResponseEntity.badRequest().body("Only consultants can add portfolio projects");
            }

            PortfolioProject project = portfolioService.addProjectToPortfolio((Consultant) user, projectRequest);
            return ResponseEntity.ok(project);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
                String fileUrl = minioService.uploadFile(file, "Portfolio");
                uploadedUrls.add(fileUrl);
            }

            return ResponseEntity.ok(Map.of("imageUrls", uploadedUrls));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

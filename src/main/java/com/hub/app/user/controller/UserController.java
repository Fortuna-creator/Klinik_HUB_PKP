package com.hub.app.user.controller;

import com.hub.app.auth.service.JwtService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.user.dto.ConsultantListResponse;
import com.hub.app.user.dto.PortfolioRequest;
import com.hub.app.user.dto.ProfileUpdateRequest;
import com.hub.app.user.dto.ProfileUpdateResponse;
import com.hub.app.user.model.ConsultantType;
import com.hub.app.user.model.Portfolio;
import com.hub.app.user.model.Role;
import com.hub.app.user.model.User;
import com.hub.app.user.repository.UserRepository;
import com.hub.app.user.service.ConsultantService;
import com.hub.app.user.service.HomeOwnerService;
import com.hub.app.user.service.PortfolioService;
import com.hub.app.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
    public ResponseEntity<?> getPortfolio(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            return portfolioService.getPortfolioByUser(user)
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
}

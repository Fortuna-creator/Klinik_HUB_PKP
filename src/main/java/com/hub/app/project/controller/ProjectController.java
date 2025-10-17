package com.hub.app.project.controller;

import com.hub.app.auth.service.JwtService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.DomainException;
import com.hub.app.project.dto.*;
import com.hub.app.project.model.ProjectStatus;
import com.hub.app.project.model.ProjectType;
import com.hub.app.project.service.ProjectService;
import com.hub.app.user.model.User;
import com.hub.app.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ApiErrorBuilder apiErrorBuilder;

    @PostMapping
    public ResponseEntity<?> createProject(@RequestHeader("Authorization") String authHeader,
                                           @Valid @RequestBody CreateProjectRequest request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            CreateProjectResponse response = projectService.createProject(request, user);
            return ResponseEntity.status(201).body(response);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getProjects(@RequestHeader("Authorization") String authHeader,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size,
                                         @RequestParam(required = false) ProjectType type,
                                         @RequestParam(required = false) ProjectStatus status) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            ProjectListWrapper response = projectService.getUserProjects(user, page, size, type, status);
            return ResponseEntity.ok(response);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectDetail(@PathVariable UUID id,
                                              @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            ProjectDetailResponse response = projectService.getProjectDetail(id, user);
            return ResponseEntity.ok(response);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
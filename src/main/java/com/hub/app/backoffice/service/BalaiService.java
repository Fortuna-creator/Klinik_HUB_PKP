package com.hub.app.backoffice.service;

import com.hub.app.auth.model.ActivationToken;
import com.hub.app.auth.repository.ActivationTokenRepository;
import com.hub.app.backoffice.dto.RegisterConsultantRequest;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.user.model.Role;
import com.hub.app.user.model.User;
import com.hub.app.user.model.UserActivityStatus;
import com.hub.app.user.model.VerificationStatus;
import com.hub.app.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalaiService {

    private final UserRepository userRepo;
    private final ActivationTokenRepository activationTokenRepo;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public List<User> getUsersForVerification() {
        return userRepo.findAll().stream()
                .filter(u -> u.getStatus() == VerificationStatus.VERIFICATION_IN_PROCESS)
                .toList();
    }

    /**
     * Approve user homeowner registration
     */
    public User approveUser(Long userId) {
        var user = userRepo.findById(userId).orElseThrow();
        user.setStatus(VerificationStatus.VERIFIED);
        return userRepo.save(user);
    }

    /**
     * Reject user homeowner registration
     */
    public User rejectUser(Long userId) {
        var user = userRepo.findById(userId).orElseThrow();
        user.setStatus(VerificationStatus.REJECTED);
        return userRepo.save(user);
    }

    public User backofficeDeactivateUser(Long userId) {
        var user = userRepo.findById(userId).orElseThrow();
        user.setActivityStatus(UserActivityStatus.INACTIVE);
        return userRepo.save(user);
    }

    /**
     * Register one consultant/supervisor
     */
    @Transactional
    public User registerConsultant(RegisterConsultantRequest req) {
        // Validate required fields
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            throw new DomainException(ErrorCode.BAD_REQUEST, "Name is required");
        }
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new DomainException(ErrorCode.BAD_REQUEST, "Email is required");
        }
        if (req.getPhone() == null || req.getPhone().trim().isEmpty()) {
            throw new DomainException(ErrorCode.BAD_REQUEST, "Phone is required");
        }

        // Check for duplicate email
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new DomainException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        // Validate email format (basic check)
        if (!req.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new DomainException(ErrorCode.BAD_REQUEST, "Invalid email format");
        }

        User u = User.builder()
                .name(req.getName().trim())
                .email(req.getEmail().trim().toLowerCase())
                .phone(req.getPhone().trim())
                .password(UUID.randomUUID().toString()) // temporary random password, will be changed during activation
                .role(req.isProfessional() ? Role.CONSULTANT : Role.SUPERVISOR)
                .status(VerificationStatus.UNVERIFIED) // inactive until email activation
                .activityStatus(UserActivityStatus.INACTIVE)
                .build();
        userRepo.save(u);

        log.info("Consultant registered by balai: email={}, role={}", u.getEmail(), u.getRole());

        sendActivationEmail(u);
        return u;
    }

    @Transactional
    public List<User> bulkRegisterFromCsv(MultipartFile file) {
        List<User> registered = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) { // skip header
                    headerSkipped = true;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 5) {
                    throw new RuntimeException("Invalid CSV format, expected 5 columns");
                }

                RegisterConsultantRequest req = new RegisterConsultantRequest();
                req.setName(parts[0].trim());
                req.setEmail(parts[1].trim());
                req.setPhone(parts[2].trim());
                req.setUniversity(parts[3].trim().isEmpty() ? null : parts[3].trim());
                req.setProfessional(Boolean.parseBoolean(parts[4].trim()));

                registered.add(registerConsultant(req));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing CSV file", e);
        }
        return registered;
    }

    /**
     * Send activation email with secure activation link
     */
    private void sendActivationEmail(User u) {
        // Generate secure activation token with expiration
        String activationToken = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS); // 24 hours expiration

        // Store activation token
        ActivationToken token = ActivationToken.builder()
                .userId(u.getId())
                .token(activationToken)
                .expiresAt(expiresAt)
                .used(false)
                .build();
        activationTokenRepo.save(token);

        var msg = new org.springframework.mail.SimpleMailMessage();
        msg.setTo(u.getEmail());
        msg.setSubject("Aktivasi Akun Konsultan/Pengawas - HUB");
        msg.setText("Halo " + u.getName() +
                ",\n\nAkun Anda telah didaftarkan oleh Balai.\n" +
                "Silakan aktivasi akun Anda dengan mengunjungi link berikut dalam 24 jam:\n" +
                "http://hub-app/activate?email=" + u.getEmail() + "&token=" + activationToken +
                "\n\nJika link tidak dapat diklik, salin dan tempel ke browser Anda.\n" +
                "Jika Anda tidak meminta pendaftaran ini, abaikan email ini.\n\n" +
                "Terima kasih.\n\n" +
                "Tim HUB");
        try {
            mailSender.send(msg);
            log.info("Activation email sent to: {}", u.getEmail());
        } catch (Exception e) {
            log.error("Failed to send activation email to: {}", u.getEmail(), e);
            throw new DomainException(ErrorCode.INTERNAL_ERROR, "Failed to send activation email");
        }
    }
}

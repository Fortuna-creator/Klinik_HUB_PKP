package com.hub.app.backoffice.service;

import com.hub.app.backoffice.dto.RegisterConsultantRequest;
import com.hub.app.backoffice.dto.BulkRegisterConsultantRequest;
import com.hub.app.user.model.Role;
import com.hub.app.user.model.User;
import com.hub.app.user.model.UserStatus;
import com.hub.app.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BalaiService {

    private final UserRepository userRepo;
    private final JavaMailSender mailSender;

    /**
     * Approve user homeowner registration
     */
    public User approveUser(Long userId) {
        var user = userRepo.findById(userId).orElseThrow();
        user.setStatus(UserStatus.ACTIVE);
        return userRepo.save(user);
    }

    /**
     * Reject user homeowner registration
     */
    public User rejectUser(Long userId) {
        var user = userRepo.findById(userId).orElseThrow();
        user.setStatus(UserStatus.REJECTED);
        return userRepo.save(user);
    }

    /**
     * Register one consultant/supervisor
     */
    @Transactional
    public User registerConsultant(RegisterConsultantRequest req) {
        User u = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .password(UUID.randomUUID().toString()) // sementara random, nanti ganti di aktivasi
                .role(req.isProfessional() ? Role.CONSULTANT : Role.SUPERVISOR)
                .status(UserStatus.PENDING_APPROVAL) // pending sampai aktivasi email
                .build();
        userRepo.save(u);

        sendActivationEmail(u);
        return u;
    }

    /**
     * Bulk register consultants/supervisors
     */
    @Transactional
    public List<User> bulkRegister(BulkRegisterConsultantRequest bulkReq) {
        List<User> registered = new ArrayList<>();
        for (RegisterConsultantRequest req : bulkReq.getConsultants()) {
            registered.add(registerConsultant(req));
        }
        return registered;
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
     * Send activation email with activation link
     */
    private void sendActivationEmail(User u) {
        var msg = new org.springframework.mail.SimpleMailMessage();
        msg.setTo(u.getEmail());
        msg.setSubject("Aktivasi Akun Konsultan/Pengawas - HUB");
        msg.setText("Halo " + u.getName() +
                ",\n\nAkun Anda telah didaftarkan oleh Balai.\n" +
                "Silakan aktivasi akun Anda dengan mengunjungi link berikut:\n" +
                "http://hub-app/activate?email=" + u.getEmail() + "&token=" + UUID.randomUUID() +
                "\n\nTerima kasih.");
        mailSender.send(msg);
    }
}

package com.hub.app.backoffice.controller;

import com.hub.app.backoffice.dto.RegisterConsultantRequest;
import com.hub.app.backoffice.service.BalaiService;
import com.hub.app.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/balai")
@RequiredArgsConstructor
public class BalaiController {
    private final BalaiService balaiService;

    @GetMapping("/verification-pending")
    public ResponseEntity<List<User>> getUsersForVerification() {
        return ResponseEntity.ok(balaiService.getUsersForVerification());
    }

    @PostMapping("/approve/{userId}")
    public ResponseEntity<User> approveUser(@PathVariable Long userId) {
        return ResponseEntity.ok(balaiService.approveUser(userId));
    }

    @PostMapping("/reject/{userId}")
    public ResponseEntity<User> rejectUser(@PathVariable Long userId) {
        return ResponseEntity.ok(balaiService.rejectUser(userId));
    }

    @PostMapping("/deactivate/{userId}")
    public ResponseEntity<User> deactivateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(balaiService.backofficeDeactivateUser(userId));
    }

    @PostMapping("/register-consultant")
    public ResponseEntity<User> registerConsultant(@RequestBody RegisterConsultantRequest req) {
        return ResponseEntity.ok(balaiService.registerConsultant(req));
    }


    @PostMapping(value = "/register-consultant/bulk", consumes = "multipart/form-data")
    public ResponseEntity<List<User>> bulkRegisterFromCsv(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(balaiService.bulkRegisterFromCsv(file));
    }

}

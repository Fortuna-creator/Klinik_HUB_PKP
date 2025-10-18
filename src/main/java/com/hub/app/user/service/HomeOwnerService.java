package com.hub.app.user.service;

import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.user.dto.ProfileUpdateRequest;
import com.hub.app.user.dto.ProfileUpdateResponse;
import com.hub.app.user.model.HomeOwner;
import com.hub.app.user.model.User;
import com.hub.app.user.model.VerificationStatus;
import com.hub.app.user.repository.HomeOwnerRepository;
import com.hub.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeOwnerService {

    private final HomeOwnerRepository homeOwnerRepository;
    private final UserRepository userRepository;

    public List<HomeOwner> getAllHomeOwners() {
        return homeOwnerRepository.findAll();
    }

    public HomeOwner findByEmail(String email) {
        return homeOwnerRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public ProfileUpdateResponse updateProfile(Long userId, ProfileUpdateRequest req) {
        HomeOwner homeOwner = homeOwnerRepository.findById(userId).orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));

        // Check if user is eligible for profile update
        if (homeOwner.getStatus() != VerificationStatus.UNVERIFIED && homeOwner.getStatus() != VerificationStatus.REJECTED) {
            throw new DomainException(ErrorCode.BAD_REQUEST, "HomeOwner not eligible for profile update");
        }

        // Check KTP number uniqueness
        if (userRepository.existsByKtpNumberAndIdNot(req.getKtpNumber(), userId)) {
            throw new DomainException(ErrorCode.KTP_ALREADY_USED);
        }

        // Update fields
        homeOwner.setName(req.getFullName());
        homeOwner.setEmail(req.getEmail());
        homeOwner.setPhone(req.getPhone());
        homeOwner.setAddress(req.getAddress());
        homeOwner.setAvatarUrl(req.getAvatarUrl());
        homeOwner.setBio(req.getBio());
        homeOwner.setKtpNumber(req.getKtpNumber());
        homeOwner.setKtpUrl(req.getKtpImageUrl());
        homeOwner.setUpdatedAt(Instant.now());
        homeOwner.setStatus(VerificationStatus.VERIFICATION_IN_PROCESS);

        HomeOwner saved = homeOwnerRepository.save(homeOwner);

        return ProfileUpdateResponse.builder()
                .userId(saved.getId())
                .fullName(saved.getName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .address(saved.getAddress())
                .avatarUrl(saved.getAvatarUrl())
                .bio(saved.getBio())
                .ktpNumber(saved.getKtpNumber())
                .ktpImageUrl(saved.getKtpUrl())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

}
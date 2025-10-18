package com.hub.app.user.service;

import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.user.dto.ProfileUpdateRequest;
import com.hub.app.user.dto.ProfileUpdateResponse;
import com.hub.app.user.model.HomeOwner;
import com.hub.app.user.model.VerificationStatus;
import com.hub.app.user.repository.HomeOwnerRepository;
import com.hub.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeOwnerServiceTest {

    @Mock
    private HomeOwnerRepository homeOwnerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HomeOwnerService homeOwnerService;

    private HomeOwner testHomeOwner;
    private ProfileUpdateRequest testRequest;

    @BeforeEach
    void setUp() {
        testHomeOwner = new HomeOwner();
        testHomeOwner.setId(1L);
        testHomeOwner.setName("John Doe");
        testHomeOwner.setEmail("john@example.com");
        testHomeOwner.setPhone("1234567890");
        testHomeOwner.setAddress("123 Main St");
        testHomeOwner.setAvatarUrl("avatar.jpg");
        testHomeOwner.setBio("Test bio");
        testHomeOwner.setKtpNumber("1234567890123456");
        testHomeOwner.setKtpUrl("ktp.jpg");
        testHomeOwner.setStatus(VerificationStatus.UNVERIFIED);
        testHomeOwner.setUpdatedAt(Instant.now());

        testRequest = new ProfileUpdateRequest();
        testRequest.setFullName("Jane Doe");
        testRequest.setEmail("jane@example.com");
        testRequest.setPhone("0987654321");
        testRequest.setAddress("456 Oak St");
        testRequest.setAvatarUrl("new-avatar.jpg");
        testRequest.setBio("Updated bio");
        testRequest.setKtpNumber("6543210987654321");
        testRequest.setKtpImageUrl("new-ktp.jpg");
    }

    @Test
    void getAllHomeOwners_ShouldReturnAllHomeOwners() {
        List<HomeOwner> expectedHomeOwners = List.of(testHomeOwner);
        when(homeOwnerRepository.findAll()).thenReturn(expectedHomeOwners);

        List<HomeOwner> result = homeOwnerService.getAllHomeOwners();

        assertEquals(expectedHomeOwners, result);
        verify(homeOwnerRepository).findAll();
    }

    @Test
    void findByEmail_ShouldReturnHomeOwnerWhenFound() {
        when(homeOwnerRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testHomeOwner));

        HomeOwner result = homeOwnerService.findByEmail("john@example.com");

        assertEquals(testHomeOwner, result);
        verify(homeOwnerRepository).findByEmail("john@example.com");
    }

    @Test
    void findByEmail_ShouldReturnNullWhenNotFound() {
        when(homeOwnerRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        HomeOwner result = homeOwnerService.findByEmail("notfound@example.com");

        assertNull(result);
        verify(homeOwnerRepository).findByEmail("notfound@example.com");
    }

    @Test
    void updateProfile_ShouldUpdateProfileSuccessfully() {
        when(homeOwnerRepository.findById(1L)).thenReturn(Optional.of(testHomeOwner));
        when(userRepository.existsByKtpNumberAndIdNot("6543210987654321", 1L)).thenReturn(false);
        when(homeOwnerRepository.save(any(HomeOwner.class))).thenReturn(testHomeOwner);

        ProfileUpdateResponse result = homeOwnerService.updateProfile(1L, testRequest);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("Jane Doe", result.getFullName());
        assertEquals("jane@example.com", result.getEmail());
        assertEquals("0987654321", result.getPhone());
        assertEquals("456 Oak St", result.getAddress());
        assertEquals("new-avatar.jpg", result.getAvatarUrl());
        assertEquals("Updated bio", result.getBio());
        assertEquals("6543210987654321", result.getKtpNumber());
        assertEquals("new-ktp.jpg", result.getKtpImageUrl());
        assertNotNull(result.getUpdatedAt());

        verify(homeOwnerRepository).findById(1L);
        verify(userRepository).existsByKtpNumberAndIdNot("6543210987654321", 1L);
        verify(homeOwnerRepository).save(testHomeOwner);
    }

    @Test
    void updateProfile_ShouldThrowExceptionWhenHomeOwnerNotFound() {
        when(homeOwnerRepository.findById(1L)).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class,
            () -> homeOwnerService.updateProfile(1L, testRequest));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(homeOwnerRepository).findById(1L);
        verify(homeOwnerRepository, never()).save(any(HomeOwner.class));
    }

    @Test
    void updateProfile_ShouldThrowExceptionWhenHomeOwnerNotEligible() {
        testHomeOwner.setStatus(VerificationStatus.VERIFIED);
        when(homeOwnerRepository.findById(1L)).thenReturn(Optional.of(testHomeOwner));

        DomainException exception = assertThrows(DomainException.class,
            () -> homeOwnerService.updateProfile(1L, testRequest));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        verify(homeOwnerRepository).findById(1L);
        verify(homeOwnerRepository, never()).save(any(HomeOwner.class));
    }

    @Test
    void updateProfile_ShouldThrowExceptionWhenKtpNumberAlreadyExists() {
        when(homeOwnerRepository.findById(1L)).thenReturn(Optional.of(testHomeOwner));
        when(userRepository.existsByKtpNumberAndIdNot("6543210987654321", 1L)).thenReturn(true);

        DomainException exception = assertThrows(DomainException.class,
            () -> homeOwnerService.updateProfile(1L, testRequest));

        assertEquals(ErrorCode.KTP_ALREADY_USED, exception.getErrorCode());
        verify(homeOwnerRepository).findById(1L);
        verify(userRepository).existsByKtpNumberAndIdNot("6543210987654321", 1L);
        verify(homeOwnerRepository, never()).save(any(HomeOwner.class));
    }

    @Test
    void updateProfile_ShouldUpdateStatusToVerificationInProcess() {
        when(homeOwnerRepository.findById(1L)).thenReturn(Optional.of(testHomeOwner));
        when(userRepository.existsByKtpNumberAndIdNot("6543210987654321", 1L)).thenReturn(false);
        when(homeOwnerRepository.save(any(HomeOwner.class))).thenReturn(testHomeOwner);

        homeOwnerService.updateProfile(1L, testRequest);

        verify(homeOwnerRepository).save(argThat(homeOwner ->
            homeOwner.getStatus() == VerificationStatus.VERIFICATION_IN_PROCESS));
    }

    @Test
    void updateProfile_ShouldUpdateUpdatedAtTimestamp() {
        when(homeOwnerRepository.findById(1L)).thenReturn(Optional.of(testHomeOwner));
        when(userRepository.existsByKtpNumberAndIdNot("6543210987654321", 1L)).thenReturn(false);
        when(homeOwnerRepository.save(any(HomeOwner.class))).thenReturn(testHomeOwner);

        homeOwnerService.updateProfile(1L, testRequest);

        verify(homeOwnerRepository).save(argThat(homeOwner ->
            homeOwner.getUpdatedAt() != null));
    }

    @Test
    void updateProfile_ShouldHandleNullOptionalFields() {
        testRequest.setAvatarUrl(null);
        testRequest.setBio(null);

        when(homeOwnerRepository.findById(1L)).thenReturn(Optional.of(testHomeOwner));
        when(userRepository.existsByKtpNumberAndIdNot("6543210987654321", 1L)).thenReturn(false);
        when(homeOwnerRepository.save(any(HomeOwner.class))).thenReturn(testHomeOwner);

        ProfileUpdateResponse result = homeOwnerService.updateProfile(1L, testRequest);

        assertNotNull(result);
        verify(homeOwnerRepository).save(any(HomeOwner.class));
    }

    @Test
    void updateProfile_ShouldBeTransactional() {
        // Verify that the method has @Transactional annotation
        try {
            assertTrue(HomeOwnerService.class.getMethod("updateProfile", Long.class, ProfileUpdateRequest.class)
                .isAnnotationPresent(Transactional.class));
        } catch (NoSuchMethodException e) {
            fail("Method should exist", e);
        }
    }
}
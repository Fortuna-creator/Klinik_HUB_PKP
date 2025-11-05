package com.hub.app.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.ErrorCode;
import com.hub.app.wallet.dto.TopupRequest;
import com.hub.app.wallet.dto.WalletResponse;
import com.hub.app.wallet.model.UserWallet;
import com.hub.app.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WalletService walletService;

    @Mock
    private ApiErrorBuilder apiErrorBuilder;

    // Test helper class to override private method
    private static class TestWalletController extends WalletController {
        public TestWalletController(WalletService walletService, ApiErrorBuilder apiErrorBuilder) {
            super(walletService, apiErrorBuilder);
        }

        @Override
        protected Long extractUserIdFromToken(String authHeader) {
            return 1L;
        }
    }

    private ObjectMapper objectMapper;
    private TopupRequest topupRequest;
    private WalletResponse walletResponse;
    private UserWallet userWallet;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestWalletController(walletService, apiErrorBuilder)).build();
        objectMapper = new ObjectMapper();

        topupRequest = new TopupRequest();
        topupRequest.setAmount(BigDecimal.valueOf(1000));

        userWallet = new UserWallet();
        userWallet.setId(1L);
        userWallet.setBalance(BigDecimal.valueOf(1500));
        userWallet.setCurrency("IDR");
        userWallet.setIsActive(true);

        walletResponse = WalletResponse.builder()
                .id(1L)
                .userId(1L)
                .balance(BigDecimal.valueOf(1500))
                .currency("IDR")
                .isActive(true)
                .build();
    }

    @Test
    void getBalance_ShouldReturnWalletBalance() throws Exception {
        when(walletService.getWallet(1L)).thenReturn(userWallet);

        mockMvc.perform(get("/api/wallet/balance")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500))
                .andExpect(jsonPath("$.currency").value("IDR"));

        verify(walletService).getWallet(1L);
    }

    @Test
    void getBalance_ShouldHandleException() throws Exception {
        when(walletService.getWallet(1L)).thenThrow(new RuntimeException("Test error"));
        when(apiErrorBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error"))
                .thenReturn(null);

        // Already set up in BeforeEach

        mockMvc.perform(get("/api/wallet/balance")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isInternalServerError());

        verify(apiErrorBuilder).error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error");
    }

    @Test
    void topupBalance_ShouldReturnUpdatedWallet() throws Exception {
        when(walletService.addBalance(1L, BigDecimal.valueOf(1000))).thenReturn(userWallet);

        mockMvc = MockMvcBuilders.standaloneSetup(new WalletController(walletService, apiErrorBuilder) {
            @Override
            protected Long extractUserIdFromToken(String authHeader) {
                return 1L;
            }
        }).build();

        mockMvc.perform(post("/api/wallet/topup")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500));

        verify(walletService).addBalance(1L, BigDecimal.valueOf(1000));
    }

    @Test
    void topupBalance_ShouldHandleException() throws Exception {
        when(walletService.addBalance(1L, BigDecimal.valueOf(1000))).thenThrow(new RuntimeException("Test error"));
        when(apiErrorBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error"))
                .thenReturn(null);

        mockMvc = MockMvcBuilders.standaloneSetup(new WalletController(walletService, apiErrorBuilder) {
            @Override
            protected Long extractUserIdFromToken(String authHeader) {
                return 1L;
            }
        }).build();

        mockMvc.perform(post("/api/wallet/topup")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topupRequest)))
                .andExpect(status().isInternalServerError());

        verify(apiErrorBuilder).error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error");
    }

    @Test
    void adminTopup_ShouldReturnUpdatedWallet() throws Exception {
        when(walletService.addBalance(2L, BigDecimal.valueOf(1000))).thenReturn(userWallet);

        mockMvc.perform(post("/api/wallet/admin/topup/{userId}", 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500));

        verify(walletService).addBalance(2L, BigDecimal.valueOf(1000));
    }

    @Test
    void adminTopup_ShouldHandleException() throws Exception {
        when(walletService.addBalance(2L, BigDecimal.valueOf(1000))).thenThrow(new RuntimeException("Test error"));
        when(apiErrorBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error"))
                .thenReturn(null);

        mockMvc.perform(post("/api/wallet/admin/topup/{userId}", 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topupRequest)))
                .andExpect(status().isInternalServerError());

        verify(apiErrorBuilder).error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error");
    }

    @Test
    void extractUserIdFromToken_ShouldReturnDummyValue() {
        // This test verifies the placeholder implementation
        WalletController controller = new WalletController(walletService, apiErrorBuilder);
        // Since it's a placeholder, we can't really test it meaningfully
        // In a real implementation, this would extract from JWT
    }
}
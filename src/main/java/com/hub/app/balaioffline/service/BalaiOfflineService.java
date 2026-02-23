package com.hub.app.balaioffline.service;

import com.hub.app.balaioffline.dto.BalaiRequest;
import com.hub.app.balaioffline.dto.BalaiResponse;
import com.hub.app.balaioffline.model.Balai;
import com.hub.app.balaioffline.repository.BalaiRepository;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalaiOfflineService {

    private static final Logger logger = LoggerFactory.getLogger(BalaiOfflineService.class);
    private final BalaiRepository balaiRepository;

    @Transactional(readOnly = true)
    public List<BalaiResponse> getAllBalai() {
        logger.info("Fetching all balai");
        List<Balai> balaiList = balaiRepository.findAll();
        logger.info("Found {} balai records", balaiList.size());

        return balaiList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BalaiResponse getBalaiById(UUID id) {
        logger.info("Fetching balai with id: {}", id);
        Balai balai = balaiRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Balai not found with id: {}", id);
                    return new DomainException(ErrorCode.BALAI_NOT_FOUND, "Balai not found.");
                });

        logger.info("Successfully retrieved balai: {}", balai.getName());
        return mapToResponse(balai);
    }

    @Transactional
    public BalaiResponse createBalai(BalaiRequest request) {
        logger.info("Creating new balai with name: {}", request.getName());

        Balai balai = new Balai();
        balai.setName(request.getName());
        balai.setDescription(request.getDescription());
        balai.setWebUrl(request.getWebUrl());
        balai.setEmail(request.getEmail());
        balai.setPhoneNumber(request.getPhoneNumber());
        balai.setImageUrl(request.getImageUrl());
        balai.setAddress(request.getAddress());

        Balai savedBalai = balaiRepository.save(balai);
        logger.info("Successfully created balai with id: {}", savedBalai.getId());

        return mapToResponse(savedBalai);
    }

    @Transactional
    public BalaiResponse updateBalai(UUID id, BalaiRequest request) {
        logger.info("Updating balai with id: {}", id);

        Balai balai = balaiRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Balai not found with id: {}", id);
                    return new DomainException(ErrorCode.BALAI_NOT_FOUND, "Balai not found.");
                });

        balai.setName(request.getName());
        balai.setDescription(request.getDescription());
        balai.setWebUrl(request.getWebUrl());
        balai.setEmail(request.getEmail());
        balai.setPhoneNumber(request.getPhoneNumber());
        balai.setImageUrl(request.getImageUrl());
        balai.setAddress(request.getAddress());

        Balai updatedBalai = balaiRepository.save(balai);
        logger.info("Successfully updated balai: {}", updatedBalai.getName());

        return mapToResponse(updatedBalai);
    }

    @Transactional
    public void deleteBalai(UUID id) {
        logger.info("Deleting balai with id: {}", id);

        Balai balai = balaiRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Balai not found with id: {}", id);
                    return new DomainException(ErrorCode.BALAI_NOT_FOUND, "Balai not found.");
                });

        balaiRepository.delete(balai);
        logger.info("Successfully deleted balai: {}", balai.getName());
    }

    private BalaiResponse mapToResponse(Balai balai) {
        return BalaiResponse.builder()
                .id(balai.getId())
                .name(balai.getName())
                .description(balai.getDescription())
                .webUrl(balai.getWebUrl())
                .email(balai.getEmail())
                .phoneNumber(balai.getPhoneNumber())
                .imageUrl(balai.getImageUrl())
                .address(balai.getAddress())
                .createdAt(balai.getCreatedAt())
                .updatedAt(balai.getUpdatedAt())
                .build();
    }
}


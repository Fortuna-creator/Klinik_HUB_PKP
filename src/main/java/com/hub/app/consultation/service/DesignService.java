package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.DesignResponse;
import com.hub.app.consultation.model.Design;
import com.hub.app.consultation.model.DesignStatus;
import com.hub.app.consultation.model.DesignType;
import com.hub.app.consultation.repository.DesignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesignService {

    private final DesignRepository designRepository;

    public List<DesignResponse> getPrototypeDesigns() {
        List<Design> designs = designRepository.findPrototypeDesigns(DesignType.PROTOTYPE, DesignStatus.DRAFT);
        return designs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DesignResponse getDesignById(UUID id) {
        Design design = designRepository.findById(id)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.DESIGN_NOT_FOUND));
        return mapToResponse(design);
    }

    public DesignResponse selectDesign(UUID id) {
        Design design = designRepository.findById(id)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.DESIGN_NOT_FOUND));
        design.setStatus(DesignStatus.DIPILIH);
        designRepository.save(design);
        return mapToResponse(design);
    }

    public Design getDesignEntityById(UUID id) {
        return designRepository.findById(id)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.DESIGN_NOT_FOUND));
    }

    private DesignResponse mapToResponse(Design design) {
        return DesignResponse.builder()
                .id(design.getId())
                .name(design.getName())
                .type(design.getType())
                .category(design.getCategory())
                .description(design.getDescription())
                .thumbnailUrl(design.getThumbnailUrl())
                .fileDedUrl(design.getFileDedUrl())
                .fileRabUrl(design.getFileRabUrl())
                .fileSpektekUrl(design.getFileSpektekUrl())
                .status(design.getStatus())
                .createdAt(design.getCreatedAt())
                .updatedAt(design.getUpdatedAt())
                .build();
    }
}
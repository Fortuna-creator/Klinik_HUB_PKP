package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.FavoriteDesignRequest;
import com.hub.app.consultation.dto.FavoriteDesignResponse;
import com.hub.app.consultation.model.FavoriteDesign;
import com.hub.app.consultation.repository.FavoriteDesignRepository;
import com.hub.app.user.model.HomeOwner;
import com.hub.app.user.repository.HomeOwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteDesignService {

    private final FavoriteDesignRepository favoriteDesignRepository;
    private final HomeOwnerRepository homeOwnerRepository;
    private final DesignService designService;

    public FavoriteDesignResponse toggleFavorite(Long homeOwnerId, FavoriteDesignRequest request) {
        HomeOwner homeOwner = homeOwnerRepository.findById(homeOwnerId)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.HOME_OWNER_NOT_FOUND));

        // Validate design exists
        designService.getDesignById(request.getDesignId());

        Optional<FavoriteDesign> existingFavorite = favoriteDesignRepository
                .findByHomeOwnerIdAndDesignId(homeOwnerId, request.getDesignId());

        if (existingFavorite.isPresent()) {
            FavoriteDesign favorite = existingFavorite.get();
            favorite.setIsFavorited(request.getIsFavorited());
            favorite = favoriteDesignRepository.save(favorite);
            return mapToResponse(favorite);
        } else {
            FavoriteDesign favorite = new FavoriteDesign();
            favorite.setHomeOwner(homeOwner);
            favorite.setDesign(designService.getDesignEntityById(request.getDesignId()));
            favorite.setIsFavorited(request.getIsFavorited());
            favorite = favoriteDesignRepository.save(favorite);
            return mapToResponse(favorite);
        }
    }

    public List<FavoriteDesignResponse> getFavoritesByHomeOwner(Long homeOwnerId) {
        List<FavoriteDesign> favorites = favoriteDesignRepository
                .findByHomeOwnerIdAndIsFavorited(homeOwnerId, true);
        return favorites.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private FavoriteDesignResponse mapToResponse(FavoriteDesign favorite) {
        return FavoriteDesignResponse.builder()
                .id(favorite.getId())
                .homeOwnerId(favorite.getHomeOwner().getId())
                .designId(favorite.getDesign().getId())
                .isFavorited(favorite.getIsFavorited())
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}
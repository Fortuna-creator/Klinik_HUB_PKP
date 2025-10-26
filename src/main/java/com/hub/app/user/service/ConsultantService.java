package com.hub.app.user.service;

import com.hub.app.user.dto.ConsultantListResponse;
import com.hub.app.user.dto.ConsultantResponse;
import com.hub.app.user.model.Consultant;
import com.hub.app.user.model.ConsultantType;
import com.hub.app.user.repository.ConsultantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultantService {

    private final ConsultantRepository consultantRepository;

    /**
     * Get consultants near a location with pagination and optional filtering
     * @param userLat User's latitude
     * @param userLong User's longitude
     * @param page Page number (0-based)
     * @param size Page size
     * @param type Optional filter by consultant type (PROFESSIONAL or STUDENT)
     * @param specialty Optional filter by specialty
     * @param sortBy Optional sort field (name, experience, rating, distance)
     * @return ConsultantListResponse with consultants and pagination info
     */
    public ConsultantListResponse getConsultantsNearLocation(BigDecimal userLat, BigDecimal userLong, int page, int size,
                                                             ConsultantType type, String specialty, String sortBy) {
        log.info("getConsultantsNearLocation called with lat={}, long={}, page={}, size={}, type={}, specialty={}, sortBy={}",
                userLat, userLong, page, size, type, specialty, sortBy);

        try {
            // Validate coordinates
            log.debug("Validating coordinates: lat={}, long={}", userLat, userLong);
            validateCoordinates(userLat, userLong);
            log.debug("Coordinates validated successfully");

            Pageable pageable = PageRequest.of(page, size);

            // Get all consultants with location data
            log.debug("Fetching consultants with location data from repository");
            List<Consultant> allConsultants = consultantRepository.findConsultantsWithLocation();
            log.debug("Found {} consultants with location data", allConsultants.size());

            // Apply filters
            log.debug("Applying filters: type={}, specialty={}", type, specialty);
            List<Consultant> filteredConsultants = allConsultants.stream()
                    .filter(consultant -> type == null || consultant.getType() == type)
                    .filter(consultant -> specialty == null || specialty.isEmpty() ||
                            (consultant.getSpecialty() != null && consultant.getSpecialty().toLowerCase().contains(specialty.toLowerCase())))
                    .collect(Collectors.toList());
            log.debug("After filtering: {} consultants remain", filteredConsultants.size());

            // Calculate distances and create response objects
            log.debug("Calculating distances and mapping to response objects");
            List<ConsultantResponse> consultantsWithDistance = filteredConsultants.stream()
                    .map(consultant -> {
                        BigDecimal distance = calculateDistance(userLat, userLong,
                                consultant.getLatitude(), consultant.getLongitude());
                        log.trace("Consultant {} distance: {} km", consultant.getId(), distance);
                        return mapToConsultantResponse(consultant, distance);
                    })
                    .collect(Collectors.toList());
            log.debug("Mapped {} consultants with distance calculations", consultantsWithDistance.size());

            // Apply sorting
            log.debug("Applying sorting: sortBy={}", sortBy);
            if (sortBy != null && !sortBy.isEmpty()) {
                switch (sortBy.toLowerCase()) {
                    case "name":
                        consultantsWithDistance.sort(Comparator.comparing(ConsultantResponse::getFullName));
                        log.debug("Sorted by name");
                        break;
                    case "experience":
                        consultantsWithDistance.sort(Comparator.comparing(ConsultantResponse::getExperienceLevel,
                                Comparator.nullsLast(Comparator.naturalOrder())));
                        log.debug("Sorted by experience");
                        break;
                    case "rating":
                        consultantsWithDistance.sort(Comparator.comparing(ConsultantResponse::getRating,
                                Comparator.nullsLast(Comparator.reverseOrder())));
                        log.debug("Sorted by rating");
                        break;
                    case "distance":
                    default:
                        consultantsWithDistance.sort(Comparator.comparing(ConsultantResponse::getDistanceKm));
                        log.debug("Sorted by distance (default)");
                        break;
                }
            } else {
                // Default sort by distance
                consultantsWithDistance.sort(Comparator.comparing(ConsultantResponse::getDistanceKm));
                log.debug("Applied default sorting by distance");
            }

            // Apply pagination
            int total = consultantsWithDistance.size();
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, total);
            log.debug("Applying pagination: total={}, page={}, size={}, fromIndex={}, toIndex={}",
                    total, page, size, fromIndex, toIndex);

            List<ConsultantResponse> paginatedConsultants;
            if (fromIndex >= total) {
                log.warn("Page {} with size {} exceeds total results {}, returning empty list", page, size, total);
                paginatedConsultants = List.of();
            } else {
                paginatedConsultants = consultantsWithDistance.subList(fromIndex, toIndex);
                log.debug("Returning {} consultants for page {}", paginatedConsultants.size(), page);
            }

            ConsultantListResponse response = ConsultantListResponse.builder()
                    .consultants(paginatedConsultants)
                    .pagination(ConsultantListResponse.Pagination.builder()
                            .total(total)
                            .page(page)
                            .size(size)
                            .build())
                    .build();

            log.info("Successfully processed consultants near location request, returning {} consultants", paginatedConsultants.size());
            return response;
        } catch (Exception e) {
            log.error("Error in getConsultantsNearLocation with params: lat={}, long={}, page={}, size={}, type={}, specialty={}, sortBy={}",
                    userLat, userLong, page, size, type, specialty, sortBy, e);
            throw e;
        }
    }

    /**
     * Validate latitude and longitude coordinates
     */
    private void validateCoordinates(BigDecimal lat, BigDecimal lng) {
        log.debug("Validating coordinates: lat={}, lng={}", lat, lng);
        if (lat == null || lng == null) {
            log.warn("Coordinates validation failed: null values provided");
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 || lat.compareTo(BigDecimal.valueOf(90)) > 0) {
            log.warn("Latitude validation failed: {} is out of range [-90, 90]", lat);
            throw new IllegalArgumentException("Invalid latitude");
        }
        if (lng.compareTo(BigDecimal.valueOf(-180)) < 0 || lng.compareTo(BigDecimal.valueOf(180)) > 0) {
            log.warn("Longitude validation failed: {} is out of range [-180, 180]", lng);
            throw new IllegalArgumentException("Invalid longitude");
        }
        log.debug("Coordinates validation passed");
    }

    /**
     * Calculate distance between two points using Haversine formula
     * @return distance in kilometers
     */
    private BigDecimal calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        log.trace("Calculating distance between ({}, {}) and ({}, {})", lat1, lon1, lat2, lon2);

        // Since we filter consultants with location, lat2 and lon2 should not be null
        // But keeping the check for safety
        if (lat2 == null || lon2 == null) {
            log.warn("Null coordinates provided for distance calculation, returning large distance");
            return BigDecimal.valueOf(999999); // Very large distance for null coordinates
        }

        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double deltaLatRad = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double deltaLonRad = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double earthRadiusKm = 6371; // Earth's radius in kilometers
        BigDecimal distance = BigDecimal.valueOf(earthRadiusKm * c);
        log.trace("Calculated distance: {} km", distance);
        return distance;
    }

    /**
     * Map Consultant entity to ConsultantResponse DTO
     */
    private ConsultantResponse mapToConsultantResponse(Consultant consultant, BigDecimal distance) {
        log.trace("Mapping consultant {} to response DTO", consultant.getId());

        ConsultantResponse response = ConsultantResponse.builder()
                .id(consultant.getId().toString())
                .fullName(consultant.getName())
                .specialty(consultant.getSpecialty() != null ? consultant.getSpecialty() : "Arsitektur Rumah Layak Huni")
                .rating(consultant.getRating() != null ? consultant.getRating() : BigDecimal.valueOf(4.8))
                .distanceKm(distance.setScale(2, RoundingMode.HALF_UP))
                .avatarUrl(consultant.getAvatarUrl())
                .location(consultant.getLocation() != null ? consultant.getLocation() : consultant.getAddress())
                .type(consultant.getType())
                .experienceLevel(consultant.getExperienceLevel())
                .availabilityStatus(consultant.getAvailabilityStatus())
                .contactInfo(consultant.getContactInfo())
                .hourlyRate(consultant.getHourlyRate())
                .packageCost(consultant.getPackageCost())
                .build();

        log.trace("Successfully mapped consultant {} to response", consultant.getId());
        return response;
    }
}
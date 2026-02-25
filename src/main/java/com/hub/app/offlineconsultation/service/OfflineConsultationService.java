package com.hub.app.offlineconsultation.service;

import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.offlineconsultation.dto.request.*;
import com.hub.app.offlineconsultation.dto.response.*;
import com.hub.app.offlineconsultation.entity.BalaiEntity;
import com.hub.app.offlineconsultation.entity.ConsultationStatus;
import com.hub.app.offlineconsultation.entity.OfflineConsultationEntity;
import com.hub.app.offlineconsultation.repository.OcBalaiRepository;
import com.hub.app.offlineconsultation.repository.OcConsultationRepository;
import com.hub.app.user.model.User;
import com.hub.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

/**
 * Core business-logic service for everything related to offline consultations.
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li><b>Booking</b> — a Warga creates a new consultation request.</li>
 *   <li><b>My Consultations</b> — a Warga lists their own consultations.</li>
 *   <li><b>Verify</b> — a Warga checks their verification code.</li>
 *   <li><b>Dashboard list</b> — a Balai views consultations for their office.</li>
 *   <li><b>Confirm / Reject / Complete</b> — a Balai transitions a consultation
 *       through the state machine.</li>
 *   <li><b>Dashboard stats</b> — aggregated counts for today.</li>
 *   <li><b>Balai list</b> — public list of all offices.</li>
 * </ul>
 *
 * <p>This service does <b>not</b> handle idempotency checks — that responsibility
 * lives in the controllers, which call {@link IdempotencyService} before and after
 * delegating to this service.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OfflineConsultationService {

    private final OcConsultationRepository consultationRepo;
    private final OcBalaiRepository balaiRepo;
    private final UserRepository userRepo;
    private final SlotAvailabilityService slotService;

    // =====================================================================
    // 1. BOOKING (Warga)
    // =====================================================================

    /**
     * Books a new offline consultation for a Warga (citizen).
     *
     * <h4>What happens step-by-step:</h4>
     * <ol>
     *   <li>Look up the authenticated user by email (from the JWT token).</li>
     *   <li>Look up the target Balai office by its ID.</li>
     *   <li>Parse and validate the requested date (must not be in the past).</li>
     *   <li>Check if the requested time slot still has capacity — if not, throw
     *       a SLOT_UNAVAILABLE error.</li>
     *   <li>Create the consultation entity with status = menunggu_konfirmasi
     *       (waiting for confirmation).</li>
     *   <li>Save to the database and return the response DTO.</li>
     * </ol>
     *
     * @param request    the booking details from the Warga
     * @param userEmail  the email extracted from the JWT security context
     * @return a {@link BookConsultationResponse} with the new consultation details
     */
    @Transactional
    public BookConsultationResponse bookConsultation(BookConsultationRequest request, String userEmail) {

        // Step 1: Find the authenticated user
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));

        // Step 2: Find the Balai (office)
        UUID balaiId = parseUuid(request.getBalaiId(), "balaiId");
        BalaiEntity balai = balaiRepo.findById(balaiId)
                .orElseThrow(() -> new DomainException(ErrorCode.BALAI_NOT_FOUND));

        // Step 3: Parse and validate the date
        LocalDate date = parseDate(request.getDate());
        if (date.isBefore(LocalDate.now())) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_DATE,
                    "Cannot book consultation in the past");
        }

        // Step 4: Check slot availability
        if (!slotService.isSlotAvailable(balaiId, date, request.getTimeSlot())) {
            throw new DomainException(ErrorCode.OC_SLOT_UNAVAILABLE,
                    "Time slot " + request.getTimeSlot() + " is fully booked");
        }

        // Step 5: Create the entity
        OfflineConsultationEntity entity = OfflineConsultationEntity.builder()
                .user(user)
                .balai(balai)
                .consultationDate(date)
                .timeSlot(request.getTimeSlot())
                .topic(request.getTopic())
                .description(request.getDescription())
                .status(ConsultationStatus.MENUNGGU_KONFIRMASI)
                .build();

        // Step 6: Save and return
        OfflineConsultationEntity saved = consultationRepo.save(entity);
        log.info("Consultation booked: id={}, user={}, balai={}, date={}, slot={}",
                saved.getId(), user.getEmail(), balai.getName(), date, request.getTimeSlot());

        ConsultationDetailResponse detail = toDetailResponse(saved);

        return BookConsultationResponse.builder()
                .id(saved.getId().toString())
                .status(ConsultationStatus.MENUNGGU_KONFIRMASI.getValue())
                .message("Consultation booked successfully. Waiting for Balai confirmation.")
                .consultation(detail)
                .build();
    }

    // =====================================================================
    // 2. GET MY CONSULTATIONS (Warga)
    // =====================================================================

    /**
     * Returns a paginated list of the authenticated Warga's own consultations,
     * optionally filtered by status.
     *
     * <h4>Logic:</h4>
     * <ol>
     *   <li>Look up the user by email.</li>
     *   <li>If {@code statusFilter} is "all" or null, return all consultations
     *       for that user. Otherwise, filter by the requested status.</li>
     *   <li>Convert each entity to the response DTO.</li>
     * </ol>
     *
     * @param userEmail    authenticated user's email
     * @param statusFilter "all" or a specific status value like "menunggu_konfirmasi"
     * @param page         0-indexed page number
     * @param size         page size (default 10)
     * @return list of consultation details
     */
    @Transactional(readOnly = true)
    public List<ConsultationDetailResponse> getMyConsultations(
            String userEmail, String statusFilter, int page, int size) {

        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<OfflineConsultationEntity> resultPage;

        if (statusFilter == null || statusFilter.isBlank() || "all".equalsIgnoreCase(statusFilter)) {
            resultPage = consultationRepo.findByUserId(user.getId(), pageable);
        } else {
            ConsultationStatus status = ConsultationStatus.fromValue(statusFilter);
            resultPage = consultationRepo.findByUserIdAndStatus(user.getId(), status, pageable);
        }

        return resultPage.getContent().stream()
                .map(this::toDetailResponse)
                .toList();
    }

    // =====================================================================
    // 3. VERIFY CONSULTATION (Warga)
    // =====================================================================

    /**
     * Allows a Warga to verify their consultation using the verification code
     * that the Balai gave them when the session was completed.
     *
     * <h4>Logic:</h4>
     * <ol>
     *   <li>Find the consultation by ID.</li>
     *   <li>Check that the consultation belongs to the authenticated user.</li>
     *   <li>Compare the provided verification code against the stored one
     *       ({@code consultationId} field).</li>
     *   <li>Return success or throw INVALID_VERIFICATION_CODE.</li>
     * </ol>
     *
     * @param consultationId the consultation's UUID
     * @param request        contains the verification code
     * @param userEmail      authenticated user's email
     * @return verification result
     */
    @Transactional(readOnly = true)
    public VerifyConsultationResponse verifyConsultation(
            UUID consultationId, VerifyConsultationRequest request, String userEmail) {

        OfflineConsultationEntity entity = findConsultationOrThrow(consultationId);

        // Security check: only the owning Warga can verify
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));
        if (!entity.getUser().getId().equals(user.getId())) {
            throw new DomainException(ErrorCode.UNAUTHORIZED,
                    "You can only verify your own consultations");
        }

        // Compare the verification code
        if (entity.getConsultationId() == null
                || !entity.getConsultationId().equals(request.getVerificationCode())) {
            throw new DomainException(ErrorCode.OC_INVALID_VERIFICATION_CODE,
                    "Verification code is incorrect");
        }

        return VerifyConsultationResponse.builder()
                .verified(true)
                .message("Consultation verified successfully")
                .consultation(toDetailResponse(entity))
                .build();
    }

    // =====================================================================
    // 4. DASHBOARD CONSULTATIONS (Balai)
    // =====================================================================

    /**
     * Returns a paginated list of consultations for a specific Balai's dashboard,
     * optionally filtered by date and/or status.
     *
     * <h4>Filter combinations:</h4>
     * <ul>
     *   <li>balaiId only → all consultations for that Balai</li>
     *   <li>balaiId + date → consultations on that date</li>
     *   <li>balaiId + status → consultations with that status</li>
     *   <li>balaiId + date + status → narrowest filter</li>
     * </ul>
     *
     * @param balaiId      the Balai's UUID (required)
     * @param dateStr      optional date filter (YYYY-MM-DD)
     * @param statusFilter optional status filter ("all" or specific value)
     * @param page         0-indexed page number
     * @param size         page size (default 20)
     * @return list of consultation details
     */
    @Transactional(readOnly = true)
    public List<ConsultationDetailResponse> getDashboardConsultations(
            String balaiId, String dateStr, String statusFilter, int page, int size) {

        UUID balaiUuid = parseUuid(balaiId, "balaiId");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        boolean hasDate = dateStr != null && !dateStr.isBlank();
        boolean hasStatus = statusFilter != null && !statusFilter.isBlank()
                && !"all".equalsIgnoreCase(statusFilter);

        Page<OfflineConsultationEntity> resultPage;

        if (hasDate && hasStatus) {
            // Narrowest: filter by both date and status
            LocalDate date = parseDate(dateStr);
            ConsultationStatus status = ConsultationStatus.fromValue(statusFilter);
            resultPage = consultationRepo.findByBalaiIdAndDateAndStatus(balaiUuid, date, status, pageable);
        } else if (hasDate) {
            // Filter by date only
            LocalDate date = parseDate(dateStr);
            resultPage = consultationRepo.findByBalaiIdAndDate(balaiUuid, date, pageable);
        } else if (hasStatus) {
            // Filter by status only
            ConsultationStatus status = ConsultationStatus.fromValue(statusFilter);
            resultPage = consultationRepo.findByBalaiIdAndStatus(balaiUuid, status, pageable);
        } else {
            // No filters — all consultations for this Balai
            resultPage = consultationRepo.findByBalaiId(balaiUuid, pageable);
        }

        return resultPage.getContent().stream()
                .map(this::toDetailResponse)
                .toList();
    }

    // =====================================================================
    // 5. CONFIRM CONSULTATION (Balai)
    // =====================================================================

    /**
     * Balai confirms a pending consultation → status changes from
     * {@code menunggu_konfirmasi} to {@code terkonfirmasi}.
     *
     * <h4>State machine enforcement:</h4>
     * <p>The current status must be {@code menunggu_konfirmasi}. If the consultation
     * is already confirmed, completed, or cancelled, a 409 INVALID_STATE_TRANSITION
     * error is thrown. This is the core "guard" of our state machine — we never
     * allow illegal transitions.</p>
     *
     * @param consultationId the consultation's UUID
     * @param request        optional notes from the Balai
     * @return action response with updated consultation
     */
    @Transactional
    public ConsultationActionResponse confirmConsultation(
            UUID consultationId, ConfirmConsultationRequest request) {

        OfflineConsultationEntity entity = findConsultationOrThrow(consultationId);

        // Enforce state machine: only menunggu_konfirmasi → terkonfirmasi
        validateTransition(entity, ConsultationStatus.TERKONFIRMASI);

        entity.setStatus(ConsultationStatus.TERKONFIRMASI);
        entity.setNotes(request.getNotes());

        OfflineConsultationEntity saved = consultationRepo.save(entity);
        log.info("Consultation confirmed: id={}", consultationId);

        return ConsultationActionResponse.builder()
                .id(saved.getId().toString())
                .status(ConsultationStatus.TERKONFIRMASI.getValue())
                .message("Consultation confirmed successfully")
                .consultation(toDetailResponse(saved))
                .build();
    }

    // =====================================================================
    // 6. REJECT CONSULTATION (Balai)
    // =====================================================================

    /**
     * Balai rejects/cancels a consultation → status changes to {@code dibatalkan}.
     *
     * <h4>Allowed from:</h4>
     * <ul>
     *   <li>{@code menunggu_konfirmasi} → Balai rejects a pending request</li>
     *   <li>{@code terkonfirmasi} → Balai cancels an already-confirmed appointment</li>
     * </ul>
     *
     * <p>The cancellation reason (required) is stored on the entity for audit
     * and display purposes.</p>
     *
     * @param consultationId the consultation's UUID
     * @param request        contains the rejection reason
     * @return action response with updated consultation
     */
    @Transactional
    public ConsultationActionResponse rejectConsultation(
            UUID consultationId, RejectConsultationRequest request) {

        OfflineConsultationEntity entity = findConsultationOrThrow(consultationId);

        // Enforce state machine: menunggu_konfirmasi|terkonfirmasi → dibatalkan
        validateTransition(entity, ConsultationStatus.DIBATALKAN);

        entity.setStatus(ConsultationStatus.DIBATALKAN);
        entity.setCancellationReason(request.getReason());

        OfflineConsultationEntity saved = consultationRepo.save(entity);
        log.info("Consultation rejected: id={}, reason={}", consultationId, request.getReason());

        return ConsultationActionResponse.builder()
                .id(saved.getId().toString())
                .status(ConsultationStatus.DIBATALKAN.getValue())
                .message("Consultation rejected successfully")
                .consultation(toDetailResponse(saved))
                .build();
    }

    // =====================================================================
    // 7. COMPLETE CONSULTATION (Balai)
    // =====================================================================

    /**
     * Balai marks a consultation as completed and assigns a verification code.
     *
     * <h4>Flow:</h4>
     * <ol>
     *   <li>The consultation must currently be in {@code terkonfirmasi} or
     *       {@code sedang_berjalan} status.</li>
     *   <li>The Balai provides a {@code verificationId} (a code they give to the
     *       citizen in person).</li>
     *   <li>This code is stored as {@code consultationId} on the entity — the Warga
     *       can later use it via the verify endpoint.</li>
     *   <li>Status transitions to {@code selesai}.</li>
     * </ol>
     *
     * <p><b>Note:</b> Per the contract's state machine, the official path is
     * {@code terkonfirmasi → sedang_berjalan → selesai}. However, the
     * {@code sedang_berjalan} state is auto-triggered when the appointment time
     * arrives. To keep things practical, we allow completing from both
     * {@code terkonfirmasi} (if the auto-transition hasn't fired yet) and
     * {@code sedang_berjalan}.</p>
     *
     * @param consultationId the consultation's UUID
     * @param request        contains the verification code
     * @return action response with updated consultation
     */
    @Transactional
    public ConsultationActionResponse completeConsultation(
            UUID consultationId, CompleteConsultationRequest request) {

        OfflineConsultationEntity entity = findConsultationOrThrow(consultationId);

        // If still confirmed, auto-advance to sedang_berjalan first
        if (entity.getStatus() == ConsultationStatus.TERKONFIRMASI) {
            entity.setStatus(ConsultationStatus.SEDANG_BERJALAN);
        }

        // Now enforce: sedang_berjalan → selesai
        validateTransition(entity, ConsultationStatus.SELESAI);

        entity.setStatus(ConsultationStatus.SELESAI);
        entity.setConsultationId(request.getVerificationId());

        OfflineConsultationEntity saved = consultationRepo.save(entity);
        log.info("Consultation completed: id={}, verificationId={}",
                consultationId, request.getVerificationId());

        return ConsultationActionResponse.builder()
                .id(saved.getId().toString())
                .status(ConsultationStatus.SELESAI.getValue())
                .message("Consultation completed successfully")
                .consultation(toDetailResponse(saved))
                .build();
    }

    // =====================================================================
    // 8. DASHBOARD STATS (Balai)
    // =====================================================================

    /**
     * Returns aggregate counts of today's consultations for a specific Balai,
     * broken down by status.
     *
     * <h4>The dashboard shows five numbers:</h4>
     * <ul>
     *   <li><b>todayTotal</b> — all consultations today regardless of status</li>
     *   <li><b>todayPending</b> — menunggu_konfirmasi</li>
     *   <li><b>todayConfirmed</b> — terkonfirmasi</li>
     *   <li><b>todayCompleted</b> — selesai</li>
     *   <li><b>todayRejected</b> — dibatalkan</li>
     * </ul>
     *
     * @param balaiId the Balai's UUID as a string
     * @return stats DTO
     */
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(String balaiId) {
        UUID balaiUuid = parseUuid(balaiId, "balaiId");
        LocalDate today = LocalDate.now();

        long total     = consultationRepo.countByBalaiAndDate(balaiUuid, today);
        long pending   = consultationRepo.countByBalaiAndDateAndStatus(balaiUuid, today, ConsultationStatus.MENUNGGU_KONFIRMASI);
        long confirmed = consultationRepo.countByBalaiAndDateAndStatus(balaiUuid, today, ConsultationStatus.TERKONFIRMASI);
        long completed = consultationRepo.countByBalaiAndDateAndStatus(balaiUuid, today, ConsultationStatus.SELESAI);
        long rejected  = consultationRepo.countByBalaiAndDateAndStatus(balaiUuid, today, ConsultationStatus.DIBATALKAN);

        return DashboardStatsResponse.builder()
                .todayTotal(total)
                .todayPending(pending)
                .todayConfirmed(confirmed)
                .todayCompleted(completed)
                .todayRejected(rejected)
                .build();
    }

    // =====================================================================
    // 9. BALAI LIST (Public)
    // =====================================================================

    /**
     * Returns a list of all Balai offices, formatted for the dropdown/selection UI.
     * This is a public endpoint (no authentication required per the contract).
     */
    @Transactional(readOnly = true)
    public List<BalaiListItemResponse> getAllBalai() {
        return balaiRepo.findAll().stream()
                .map(this::toBalaiResponse)
                .toList();
    }

    // =====================================================================
    // PRIVATE HELPERS
    // =====================================================================

    /**
     * Finds a consultation by ID or throws a 404 error.
     */
    private OfflineConsultationEntity findConsultationOrThrow(UUID consultationId) {
        return consultationRepo.findById(consultationId)
                .orElseThrow(() -> new DomainException(ErrorCode.OC_CONSULTATION_NOT_FOUND,
                        "Consultation not found: " + consultationId));
    }

    /**
     * Enforces the state machine by checking whether the current status
     * can legally transition to the target status. If not, throws a
     * 409 INVALID_STATE_TRANSITION error.
     *
     * <p>This is the central guard that prevents illegal operations like
     * confirming an already-confirmed consultation, or completing a
     * cancelled one.</p>
     */
    private void validateTransition(OfflineConsultationEntity entity, ConsultationStatus target) {
        if (!entity.getStatus().canTransitionTo(target)) {
            String msg = String.format("Cannot transition from '%s' to '%s'",
                    entity.getStatus().getValue(), target.getValue());
            throw new DomainException(ErrorCode.OC_INVALID_STATE_TRANSITION, msg);
        }
    }

    /**
     * Parses a date string in ISO 8601 format (YYYY-MM-DD).
     * Throws a BAD_REQUEST if the format is invalid.
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new DomainException(ErrorCode.BAD_REQUEST,
                    "Invalid date format: " + dateStr + ". Expected YYYY-MM-DD");
        }
    }

    /**
     * Parses a string as UUID. Throws a BAD_REQUEST with a descriptive
     * message if the format is wrong.
     */
    private UUID parseUuid(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new DomainException(ErrorCode.BAD_REQUEST,
                    "Invalid UUID for " + fieldName + ": " + value);
        }
    }

    // ── DTO Mappers ─────────────────────────────────────────────────────

    /**
     * Converts an {@link OfflineConsultationEntity} into the
     * {@link ConsultationDetailResponse} DTO that matches the API contract shape.
     *
     * <p>This mapper eagerly accesses the lazy-loaded {@code user} and {@code balai}
     * relationships, so it must be called within a transactional context.</p>
     */
    private ConsultationDetailResponse toDetailResponse(OfflineConsultationEntity entity) {
        User user = entity.getUser();
        BalaiEntity balai = entity.getBalai();

        UserSummaryDto userDto = UserSummaryDto.builder()
                .id(String.valueOf(user.getId()))
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatarUrl())
                .build();

        return ConsultationDetailResponse.builder()
                .id(entity.getId().toString())
                .user(userDto)
                .balaiLocation(balai.getLocation() != null ? balai.getLocation() : balai.getName())
                .balaiId(balai.getId().toString())
                .date(entity.getConsultationDate().toString())
                .timeSlot(entity.getTimeSlot())
                .status(entity.getStatus().getValue())
                .consultationId(entity.getConsultationId())
                .topic(entity.getTopic())
                .description(entity.getDescription())
                .cancellationReason(entity.getCancellationReason())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null)
                .build();
    }

    /**
     * Converts a {@link BalaiEntity} into the {@link BalaiListItemResponse} DTO
     * for the GET /api/balai/list endpoint.
     */
    private BalaiListItemResponse toBalaiResponse(BalaiEntity balai) {
        WorkingHoursDto workingHours = null;
        if (balai.getWorkingHoursStart() != null && balai.getWorkingHoursEnd() != null) {
            workingHours = WorkingHoursDto.builder()
                    .start(balai.getWorkingHoursStart())
                    .end(balai.getWorkingHoursEnd())
                    .build();
        }

        return BalaiListItemResponse.builder()
                .id(balai.getId().toString())
                .name(balai.getName())
                .location(balai.getLocation())
                .phone(balai.getPhoneNumber())
                .email(balai.getEmail())
                .address(balai.getAddress())
                .workingHours(workingHours)
                .build();
    }
}

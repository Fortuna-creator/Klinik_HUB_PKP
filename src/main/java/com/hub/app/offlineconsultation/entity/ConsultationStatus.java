package com.hub.app.offlineconsultation.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * State machine for offline consultation statuses.
 *
 * Valid transitions:
 *   MENUNGGU_KONFIRMASI → TERKONFIRMASI  (Balai confirms)
 *   MENUNGGU_KONFIRMASI → DIBATALKAN     (Balai rejects)
 *   TERKONFIRMASI       → SEDANG_BERJALAN (Auto when time reached)
 *   TERKONFIRMASI       → DIBATALKAN     (Balai cancels)
 *   SEDANG_BERJALAN     → SELESAI        (Balai completes)
 */
@Getter
public enum ConsultationStatus {

    MENUNGGU_KONFIRMASI("menunggu_konfirmasi"),
    TERKONFIRMASI("terkonfirmasi"),
    SEDANG_BERJALAN("sedang_berjalan"),
    SELESAI("selesai"),
    DIBATALKAN("dibatalkan");

    private final String value;

    ConsultationStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ConsultationStatus fromValue(String value) {
        for (ConsultationStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown consultation status: " + value);
    }

    /**
     * Checks whether transitioning from this status to the given target is allowed.
     */
    public boolean canTransitionTo(ConsultationStatus target) {
        return switch (this) {
            case MENUNGGU_KONFIRMASI -> target == TERKONFIRMASI || target == DIBATALKAN;
            case TERKONFIRMASI -> target == SEDANG_BERJALAN || target == DIBATALKAN;
            case SEDANG_BERJALAN -> target == SELESAI;
            case SELESAI, DIBATALKAN -> false;
        };
    }
}

package com.hub.app.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Generic categories
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Request tidak valid"),
    BAD_REQUEST("BAD_REQUEST", HttpStatus.BAD_REQUEST, "Permintaan tidak valid"),

    INTERNAL_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Terjadi kesalahan pada server"),

    // Auth
    AUTH_INVALID_CREDENTIALS("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED, "Email atau password salah"),
    AUTH_INVALID_STATUS("ACCOUNT_INACTIVE", HttpStatus.UNAUTHORIZED, "Akun tidak aktif"),
    AUTH_INVALID_OTP("INVALID_OTP", HttpStatus.UNAUTHORIZED, "Invalid OTP"),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Token tidak valid atau tidak aktif"),

    // User-specific
    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found"),
    USER_EMAIL_ALREADY_EXISTS("USER_EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT, "Email sudah terdaftar"),
    KTP_ALREADY_USED("KTP_ALREADY_USED", HttpStatus.BAD_REQUEST, "KTP number already registered"),

    // Consultant-specific
    INVALID_COORDINATE("INVALID_COORDINATE", HttpStatus.BAD_REQUEST, "Format koordinat tidak valid"),

    // Project-specific
    PROJECT_NOT_FOUND("PROJECT_NOT_FOUND", HttpStatus.NOT_FOUND, "Project not found"),

    // Consultation-specific
    CONSULTATION_NOT_FOUND("CONSULTATION_NOT_FOUND", HttpStatus.NOT_FOUND, "Consultation not found"),
    SURVEY_SCHEDULE_NOT_FOUND("SURVEY_SCHEDULE_NOT_FOUND", HttpStatus.NOT_FOUND, "Survey schedule not found"),
    INVALID_STATUS("INVALID_STATUS", HttpStatus.BAD_REQUEST, "Invalid status transition");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}

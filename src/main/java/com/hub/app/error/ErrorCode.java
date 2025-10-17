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

    // User-specific
    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.BAD_REQUEST, "User Not Found"),
    USER_EMAIL_ALREADY_EXISTS("USER_EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT, "Email sudah terdaftar"),

    // Project-specific
    PROJECT_NOT_FOUND("PROJECT_NOT_FOUND", HttpStatus.NOT_FOUND, "Project not found"),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Unauthorized access");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}

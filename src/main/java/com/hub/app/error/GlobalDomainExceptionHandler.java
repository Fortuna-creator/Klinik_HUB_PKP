package com.hub.app.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalDomainExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainException(DomainException ex) {
        ErrorCode ec = ex.getErrorCode();
        ApiErrorResponse body = ApiErrorResponse.builder()
                .errorCode(ec.getCode())
                .message(ex.getMessage() != null ? ex.getMessage() : ec.getDefaultMessage())
                .build();
        return ResponseEntity.status(ec.getHttpStatus()).body(body);
    }
}

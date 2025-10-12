package com.hub.app.error;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {
    private final ErrorCode errorCode;

    public DomainException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public DomainException(ErrorCode errorCode, String messageOverride) {
        super(messageOverride);
        this.errorCode = errorCode;
    }
}

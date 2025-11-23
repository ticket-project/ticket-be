package com.ticket.support.exception;

public class DuplicateEmailException extends RuntimeException {

    private final ErrorType errorType;
    private final Object data;

    public DuplicateEmailException(final ErrorType errorType) {
        this(errorType, null);
    }

    public DuplicateEmailException(final ErrorType errorType, Object data) {
        super(errorType.getMessage());
        this.errorType = errorType;
        this.data = data;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public Object getData() {
        return data;
    }
}

package com.ticket.support.exception;

public class PasswordInvalidException extends RuntimeException {

    private final ErrorType errorType;
    private final Object data;

    public PasswordInvalidException(final ErrorType errorType) {
        this(errorType, null);
    }

    public PasswordInvalidException(final ErrorType errorType, Object data) {
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

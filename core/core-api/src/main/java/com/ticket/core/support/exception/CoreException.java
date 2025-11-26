package com.ticket.core.support.exception;

public class CoreException extends RuntimeException {

    private final ErrorType errorType;
    private final Object data;

    public CoreException(final ErrorType errorType) {
        this(errorType, null);
    }

    public CoreException(final ErrorType errorType, Object data) {
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

package com.ticket.core.support.exception;

public class ErrorMessage {

    private final String code;
    private final String message;
    private final Object data;

    public ErrorMessage(final String code, final String message, final Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ErrorMessage(final ErrorType errorType, final Object data) {
        this(errorType.getErrorCode().name(), errorType.getMessage(), data);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}

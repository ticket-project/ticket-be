package com.ticket.core.domain.hold.support;

public class HoldReleaseLockException extends RuntimeException {

    public HoldReleaseLockException(final String message) {
        super(message);
    }

    public HoldReleaseLockException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

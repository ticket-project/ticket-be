package com.ticket.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(final String message) {
        super(message);
    }
}

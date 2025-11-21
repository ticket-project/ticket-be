package com.ticket.support;

import com.ticket.support.exception.DuplicateEmailException;
import com.ticket.support.exception.ErrorType;
import com.ticket.support.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(ApiControllerAdvice.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        log.error("Exception = {} ", e.getMessage(), e);
        return ResponseEntity
                .status(ErrorType.DEFAULT_ERROR.getStatus())
                .body(ApiResponse.error(ErrorType.DEFAULT_ERROR));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateEmailException(DuplicateEmailException e) {
        return ResponseEntity
                .status(e.getErrorType().getStatus())
                .body(ApiResponse.error(e.getErrorType(), e.getData()));
    }
}

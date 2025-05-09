package com.PicSell_IT342.PicSell.handler;

import com.PicSell_IT342.PicSell.dto.ErrorResponse;
import com.PicSell_IT342.PicSell.exception.CustomExceptions.*;
import com.paypal.base.rest.PayPalRESTException;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        log.error("Exception Handled: Status={}, Path={}, Message={}", status, request.getRequestURI(), ex.getMessage());
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), ex, request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), ex, request);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), ex, request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), ex, request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), ex, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex, request);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex.getCause() instanceof PayPalRESTException) {
            log.error("PayPal Error Details: {}", ((PayPalRESTException) ex.getCause()).getDetails());
        }
        return buildErrorResponse(status, "Payment processing failed: " + ex.getMessage(), ex, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        String message = "Validation failed: " + errors;
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, ex, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("An unexpected error occurred on path {}", request.getRequestURI(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred. Please contact support.", ex, request);
    }
}
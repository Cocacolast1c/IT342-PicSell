package com.PicSell_IT342.PicSell.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;



public final class CustomExceptions {


    private CustomExceptions() {}

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateResourceException extends RuntimeException {
        public DuplicateResourceException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class PaymentException extends RuntimeException {
        public PaymentException(String message, Throwable cause) {
            super(message, cause);
        }
        public PaymentException(String message) {
            super(message);
        }
    }



}
package com.stockinfo.exception;

/**
 * Thrown for business-rule validation failures that are not
 * simple field-level validation — e.g. "insufficient quantity to sell",
 * "email already registered", "stock already in watchlist".
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}

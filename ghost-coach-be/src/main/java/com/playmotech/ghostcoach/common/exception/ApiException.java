package com.playmotech.ghostcoach.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.Objects;

public class ApiException extends RuntimeException {

    private final HttpStatusCode status;
    private final String code;

    public ApiException(HttpStatusCode status, String code, String message) {
        super(requireNonBlank(message, "message"));
        this.status = Objects.requireNonNull(status, "status required");
        this.code = Objects.requireNonNullElse(code, "UNKNOWN");
    }

    public static ApiException badRequest(String code, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, code, message);
    }

    public static ApiException unauthorized(String code, String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, code, message);
    }

    public static ApiException forbidden(String code, String message) {
        return new ApiException(HttpStatus.FORBIDDEN, code, message);
    }

    public static ApiException notFound(String code, String message) {
        return new ApiException(HttpStatus.NOT_FOUND, code, message);
    }

    public static ApiException conflict(String code, String message) {
        return new ApiException(HttpStatus.CONFLICT, code, message);
    }

    public static ApiException unprocessableEntity(String code, String message) {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, code, message);
    }

    public static ApiException tooManyRequests(String code, String message) {
        return new ApiException(HttpStatus.TOO_MANY_REQUESTS, code, message);
    }

    public static ApiException badGateway(String code, String message) {
        return new ApiException(HttpStatus.BAD_GATEWAY, code, message);
    }

    public static ApiException serviceUnavailable(String code, String message) {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, code, message);
    }

    public static ApiException internalError(String code, String message) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, code, message);
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be non-blank");
        }
        return value;
    }
}

package com.playmotech.ghostcoach.common.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.time.Instant;

public record ApiResponse<T>(
        int status,
        String message,
        T data,
        ErrorResponse error,
        Instant timestamp
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Successfully",
                data,
                null,
                Instant.now()
        );
    }

    public static <T> ApiResponse<T> ok(HttpStatusCode statusCode, String message, T data) {
        return new ApiResponse<>(
                statusCode.value(),
                message,
                data,
                null,
                Instant.now()
        );
    }

    public static <T> ApiResponse<T> fail(HttpStatusCode statusCode, String message, ErrorResponse error) {
        return new ApiResponse<>(
                statusCode.value(),
                message,
                null,
                error,
                Instant.now()
        );
    }
}

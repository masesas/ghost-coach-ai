package com.playmotech.ghostcoach.common.dto;

import java.util.List;

public record ErrorResponse(
        String code,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {
    }

    public static ErrorResponse of(String code) {
        return new ErrorResponse(code, null);
    }

    public static ErrorResponse of(String code, List<FieldError> fieldErrors) {
        return new ErrorResponse(code, fieldErrors);
    }
}

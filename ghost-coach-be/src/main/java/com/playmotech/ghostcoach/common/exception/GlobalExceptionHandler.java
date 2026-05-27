package com.playmotech.ghostcoach.common.exception;

import com.playmotech.ghostcoach.common.dto.ApiResponse;
import com.playmotech.ghostcoach.common.dto.ErrorResponse;
import com.playmotech.ghostcoach.config.AppConfigProp;
import com.playmotech.ghostcoach.storage.LocalStorageService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final AppConfigProp appConfigProp;

    public GlobalExceptionHandler(AppConfigProp appConfigProp) {
        this.appConfigProp = appConfigProp;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApi(ApiException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.fail(
                        ex.getStatus(),
                        ex.getMessage(),
                        ErrorResponse.of(ex.getCode())
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(),
                        fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage()))
                .toList();
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(
                        ex.getStatusCode(),
                        "Validation failed",
                        ErrorResponse.of("VALIDATION_FAILED", fields)
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        List<ErrorResponse.FieldError> fields = ex.getConstraintViolations().stream()
                .map(v -> new ErrorResponse.FieldError(
                        leafName(v.getPropertyPath()),
                        v.getMessage()))
                .toList();
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(
                        HttpStatus.BAD_REQUEST,
                        "Validation failed",
                        ErrorResponse.of("VALIDATION_FAILED", fields)
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(
                        HttpStatus.BAD_REQUEST,
                        "Malformed request body",
                        ErrorResponse.of("MALFORMED_BODY")
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value for parameter '%s'".formatted(ex.getName());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(
                        HttpStatus.BAD_REQUEST,
                        message,
                        ErrorResponse.of("INVALID_PARAMETER")
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        String message = "Required parameter '%s' is missing".formatted(ex.getParameterName());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(
                        HttpStatus.BAD_REQUEST,
                        message,
                        ErrorResponse.of("MISSING_PARAMETER")
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        String message = "Method '%s' not supported".formatted(ex.getMethod());
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.fail(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        message,
                        ErrorResponse.of("METHOD_NOT_ALLOWED")
                ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCreds(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        ErrorResponse.of("INVALID_CREDENTIALS")
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(
                        HttpStatus.FORBIDDEN,
                        "You don't have permission",
                        ErrorResponse.of("ACCESS_DENIED")
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUpload(MaxUploadSizeExceededException ex) {
        String limit = LocalStorageService.formatSize(appConfigProp.getStorage().getMaxFileSize());
        return ResponseEntity
                .status(HttpStatus.CONTENT_TOO_LARGE)
                .body(ApiResponse.fail(
                        HttpStatus.CONTENT_TOO_LARGE,
                        "File exceeds " + limit + " limit",
                        ErrorResponse.of("FILE_TOO_LARGE")
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Something went wrong",
                        ErrorResponse.of("INTERNAL_ERROR")
                ));
    }

    private static String leafName(Path propertyPath) {
        String full = propertyPath.toString();
        int dot = full.lastIndexOf('.');
        return dot >= 0 ? full.substring(dot + 1) : full;
    }
}

package com.playmotech.ghostcoach.common.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler(com.playmotech.ghostcoach.support.TestConfig.defaultAppConfigProp()))
                .build();
    }

    @Test
    @DisplayName("ApiException propagates status, message, code (status field is number)")
    void apiException() throws Exception {
        mvc.perform(get("/dummy/api-conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Test conflict"))
                .andExpect(jsonPath("$.error.code").value("TEST_CONFLICT"));
    }

    @Test
    @DisplayName("BadCredentialsException → 401 INVALID_CREDENTIALS")
    void badCreds() throws Exception {
        mvc.perform(get("/dummy/bad-creds"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("AccessDeniedException → 403 ACCESS_DENIED")
    void accessDenied() throws Exception {
        mvc.perform(get("/dummy/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("MaxUploadSizeExceededException → 413 FILE_TOO_LARGE")
    void maxUpload() throws Exception {
        mvc.perform(get("/dummy/max-upload"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.error.code").value("FILE_TOO_LARGE"));
    }

    @Test
    @DisplayName("Validation failed → 400 VALIDATION_FAILED with field errors")
    void validation() throws Exception {
        mvc.perform(post("/dummy/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("value"));
    }

    @Test
    @DisplayName("Malformed JSON body → 400 MALFORMED_BODY")
    void malformedBody() throws Exception {
        mvc.perform(post("/dummy/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MALFORMED_BODY"));
    }

    @Test
    @DisplayName("Path type mismatch (/dummy/by-id/abc) → 400 INVALID_PARAMETER")
    void typeMismatch() throws Exception {
        mvc.perform(get("/dummy/by-id/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));
    }

    @Test
    @DisplayName("Missing required @RequestParam → 400 MISSING_PARAMETER")
    void missingParam() throws Exception {
        mvc.perform(get("/dummy/needs-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MISSING_PARAMETER"))
                .andExpect(jsonPath("$.message").value("Required parameter 'q' is missing"));
    }

    @Test
    @DisplayName("PUT on POST-only endpoint → 405 METHOD_NOT_ALLOWED")
    void methodNotAllowed() throws Exception {
        mvc.perform(put("/dummy/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    @DisplayName("ConstraintViolationException → 400 VALIDATION_FAILED with leaf field name")
    void constraintViolation() throws Exception {
        mvc.perform(get("/dummy/throw-cv"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("v"));
    }

    @Test
    @DisplayName("Unhandled exception → 500 INTERNAL_ERROR generic message")
    void unhandled() throws Exception {
        mvc.perform(get("/dummy/unhandled"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Something went wrong"));
    }

    @RestController
    @RequestMapping("/dummy")
    @Validated
    static class DummyController {
        @GetMapping("/api-conflict")
        void apiConflict() {
            throw ApiException.conflict("TEST_CONFLICT", "Test conflict");
        }

        @GetMapping("/bad-creds")
        void badCreds() {
            throw new BadCredentialsException("bad");
        }

        @GetMapping("/access-denied")
        void accessDenied() {
            throw new AccessDeniedException("no");
        }

        @GetMapping("/max-upload")
        void maxUpload() {
            throw new MaxUploadSizeExceededException(5_000_000L);
        }

        @PostMapping("/validate")
        void validate(@Valid @RequestBody DummyDto dto) {
        }

        @GetMapping("/by-id/{id}")
        void byId(@org.springframework.web.bind.annotation.PathVariable Long id) {
        }

        @GetMapping("/needs-param")
        void needsParam(@RequestParam String q) {
        }

        @GetMapping("/min-param")
        void minParam(@RequestParam @Min(0) int v) {
        }

        @GetMapping("/unhandled")
        void unhandled() {
            throw new RuntimeException("boom");
        }

        // Direct trigger using a real ConstraintViolation so the handler can extract field names.
        @GetMapping("/throw-cv")
        void throwCv() {
            try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
                var violations = factory.getValidator().validate(new MinHolder(-1));
                throw new ConstraintViolationException(violations);
            }
        }
    }

    record DummyDto(@NotBlank String value) {}

    record MinHolder(@Min(0) int v) {}
}

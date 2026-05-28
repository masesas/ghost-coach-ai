package com.playmotech.ghostcoach.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiExceptionTest {

    @Test
    @DisplayName("conflict() carries status, code, message")
    void conflictFactory() {
        ApiException ex = ApiException.conflict("EMAIL_EXISTS", "Email already registered");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getCode()).isEqualTo("EMAIL_EXISTS");
        assertThat(ex.getMessage()).isEqualTo("Email already registered");
    }

    @Test
    @DisplayName("getStatus returns HttpStatusCode (not cast)")
    void getStatusReturnsCode() {
        ApiException ex = ApiException.badRequest("BAD", "bad request");
        // HttpStatus implements HttpStatusCode — semantic compare via value()
        assertThat(ex.getStatus().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("constructor rejects blank message")
    void rejectsBlankMessage() {
        assertThatThrownBy(() -> ApiException.badRequest("CODE", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("message");
    }

    @Test
    @DisplayName("constructor rejects null message")
    void rejectsNullMessage() {
        assertThatThrownBy(() -> ApiException.badRequest("CODE", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("null code defaults to UNKNOWN")
    void nullCodeDefaults() {
        ApiException ex = ApiException.badRequest(null, "msg");
        assertThat(ex.getCode()).isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName("all factory methods set correct status")
    void factoryStatuses() {
        assertThat(ApiException.badRequest("C", "m").getStatus().value()).isEqualTo(400);
        assertThat(ApiException.unauthorized("C", "m").getStatus().value()).isEqualTo(401);
        assertThat(ApiException.forbidden("C", "m").getStatus().value()).isEqualTo(403);
        assertThat(ApiException.notFound("C", "m").getStatus().value()).isEqualTo(404);
        assertThat(ApiException.conflict("C", "m").getStatus().value()).isEqualTo(409);
        assertThat(ApiException.unprocessableEntity("C", "m").getStatus().value()).isEqualTo(422);
        assertThat(ApiException.tooManyRequests("C", "m").getStatus().value()).isEqualTo(429);
        assertThat(ApiException.badGateway("C", "m").getStatus().value()).isEqualTo(502);
        assertThat(ApiException.serviceUnavailable("C", "m").getStatus().value()).isEqualTo(503);
        assertThat(ApiException.internalError("C", "m").getStatus().value()).isEqualTo(500);
    }
}

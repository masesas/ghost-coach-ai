package com.playmotech.ghostcoach.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playmotech.ghostcoach.auth.dto.AuthResponse;
import com.playmotech.ghostcoach.auth.dto.LoginRequest;
import com.playmotech.ghostcoach.auth.dto.RegisterRequest;
import com.playmotech.ghostcoach.common.exception.ApiException;
import com.playmotech.ghostcoach.common.exception.GlobalExceptionHandler;
import com.playmotech.ghostcoach.auth.dto.SportPositionValidator;
import com.playmotech.ghostcoach.support.TestConfig;
import com.playmotech.ghostcoach.systemvar.SystemVarService;
import com.playmotech.ghostcoach.user.ExperienceLevel;
import com.playmotech.ghostcoach.user.Sport;
import com.playmotech.ghostcoach.user.UserPosition;
import com.playmotech.ghostcoach.user.dto.ProfileResponse;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock AuthService authService;
    @Mock SystemVarService systemVarService;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        UserPosition userPosition = new UserPosition(systemVarService);
        lenient().when(systemVarService.isValidKey(eq("POSITION"), eq("FOOTBALL"), eq("MIDFIELDER")))
                .thenReturn(true);

        LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.setConstraintValidatorFactory(new ConstraintValidatorFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
                if (key.equals(SportPositionValidator.class)) {
                    return (T) new SportPositionValidator(userPosition);
                }
                try {
                    return key.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Cannot instantiate " + key, ex);
                }
            }

            @Override
            public void releaseInstance(ConstraintValidator<?, ?> instance) {
            }
        });
        validatorFactory.afterPropertiesSet();

        mvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setValidator(validatorFactory)
                .setControllerAdvice(new GlobalExceptionHandler(TestConfig.defaultAppConfigProp()))
                .build();
    }

    @Test
    @DisplayName("POST /auth/register valid → 201 + token")
    void registerValid() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "new@example.com", "password123", "New User",
                Sport.FOOTBALL, "MIDFIELDER", ExperienceLevel.BEGINNER);
        AuthResponse res = new AuthResponse("token-xyz",
                new ProfileResponse(1L, "new@example.com", "New User",
                        Sport.FOOTBALL, "MIDFIELDER", ExperienceLevel.BEGINNER, Instant.now()));
        when(authService.register(any(RegisterRequest.class))).thenReturn(res);

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.token").value("token-xyz"))
                .andExpect(jsonPath("$.data.user.email").value("new@example.com"));
    }

    @Test
    @DisplayName("POST /auth/register with invalid body → 400 + VALIDATION_FAILED")
    void registerInvalidBody() throws Exception {
        String body = """
                {"email":"not-email","password":"x","fullName":"","sport":"FOOTBALL",
                 "position":"","experienceLevel":"BEGINNER"}
                """;

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }

    @Test
    @DisplayName("POST /auth/register email exists → 409 EMAIL_EXISTS")
    void registerEmailExists() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "exist@example.com", "password123", "Existing User",
                Sport.FOOTBALL, "MIDFIELDER", ExperienceLevel.BEGINNER);
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(ApiException.conflict("EMAIL_EXISTS", "Email already registered"));

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("EMAIL_EXISTS"));
    }

    @Test
    @DisplayName("POST /auth/login valid → 200 + token")
    void loginValid() throws Exception {
        LoginRequest req = new LoginRequest("user@example.com", "password123");
        AuthResponse res = new AuthResponse("login-token",
                new ProfileResponse(1L, "user@example.com", "U",
                        Sport.FOOTBALL, "MIDFIELDER", ExperienceLevel.BEGINNER, Instant.now()));
        when(authService.login(any(LoginRequest.class))).thenReturn(res);

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("login-token"));
    }

    @Test
    @DisplayName("POST /auth/login bad credentials → 401 INVALID_CREDENTIALS")
    void loginBadCreds() throws Exception {
        LoginRequest req = new LoginRequest("user@example.com", "wrong");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }
}

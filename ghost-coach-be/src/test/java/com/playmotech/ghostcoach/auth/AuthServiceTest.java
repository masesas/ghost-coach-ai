package com.playmotech.ghostcoach.auth;

import com.playmotech.ghostcoach.auth.dto.AuthResponse;
import com.playmotech.ghostcoach.auth.dto.LoginRequest;
import com.playmotech.ghostcoach.auth.dto.RegisterRequest;
import com.playmotech.ghostcoach.common.exception.ApiException;
import com.playmotech.ghostcoach.security.JwtService;
import com.playmotech.ghostcoach.user.ExperienceLevel;
import com.playmotech.ghostcoach.user.Sport;
import com.playmotech.ghostcoach.user.User;
import com.playmotech.ghostcoach.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    @InjectMocks AuthService authService;

    private RegisterRequest validRegister;
    private LoginRequest validLogin;

    @BeforeEach
    void setUp() {
        validRegister = new RegisterRequest(
                "test@example.com", "password123", "Test User",
                Sport.FOOTBALL, "MIDFIELDER", ExperienceLevel.INTERMEDIATE);
        validLogin = new LoginRequest("test@example.com", "password123");
    }

    @Test
    @DisplayName("register with new email returns AuthResponse")
    void registerNewEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            u.setCreatedAt(Instant.now());
            return u;
        });
        when(jwtService.generateToken(1L, "test@example.com")).thenReturn("token-xyz");

        AuthResponse result = authService.register(validRegister);

        assertThat(result.token()).isEqualTo("token-xyz");
        assertThat(result.user().email()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register with existing email throws CONFLICT EMAIL_EXISTS")
    void registerExistingEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(validRegister))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getCode()).isEqualTo("EMAIL_EXISTS");
                });

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register race condition (DataIntegrityViolation) → CONFLICT EMAIL_EXISTS")
    void registerRaceCondition() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("unique violation"));

        assertThatThrownBy(() -> authService.register(validRegister))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getCode()).isEqualTo("EMAIL_EXISTS");
                });
    }

    @Test
    @DisplayName("register propagates unexpected exceptions (no wrap)")
    void registerPropagatesUnexpected() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenThrow(new IllegalStateException("encoder down"));

        assertThatThrownBy(() -> authService.register(validRegister))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("encoder down");
    }

    @Test
    @DisplayName("login with non-existent email → BadCredentialsException")
    void loginUnknownEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(validLogin))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("login with wrong password → BadCredentialsException")
    void loginWrongPassword() {
        User user = User.builder()
                .id(1L).email("test@example.com").passwordHash("hashed")
                .fullName("T").sport(Sport.FOOTBALL).position("M")
                .experienceLevel(ExperienceLevel.BEGINNER).build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(validLogin))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("login valid returns AuthResponse")
    void loginValid() {
        User user = User.builder()
                .id(1L).email("test@example.com").passwordHash("hashed")
                .fullName("T").sport(Sport.FOOTBALL).position("M")
                .experienceLevel(ExperienceLevel.BEGINNER)
                .createdAt(Instant.now()).build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(1L, "test@example.com")).thenReturn("token-abc");

        AuthResponse result = authService.login(validLogin);

        assertThat(result.token()).isEqualTo("token-abc");
        assertThat(result.user().email()).isEqualTo("test@example.com");
    }
}

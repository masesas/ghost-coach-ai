package com.playmotech.ghostcoach.auth.dto;

import com.playmotech.ghostcoach.systemvar.SystemVarService;
import com.playmotech.ghostcoach.user.ExperienceLevel;
import com.playmotech.ghostcoach.user.Sport;
import com.playmotech.ghostcoach.user.UserPosition;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SportPositionValidatorTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void boot() {
        SystemVarService systemVarService = Mockito.mock(SystemVarService.class);
        Mockito.when(systemVarService.isValidKey("POSITION", "FOOTBALL", "MIDFIELDER")).thenReturn(true);
        Mockito.when(systemVarService.isValidKey("POSITION", "FOOTBALL", "QUARTERBACK")).thenReturn(false);
        Mockito.when(systemVarService.isValidKey("POSITION", "BASKETBALL", "CENTER")).thenReturn(true);
        Mockito.when(systemVarService.isValidKey("POSITION", "CRICKET", "GOALKEEPER")).thenReturn(false);

        UserPosition userPosition = new UserPosition(systemVarService);

        factory = Validation.byDefaultProvider()
                .configure()
                .constraintValidatorFactory(new ConstraintValidatorFactory() {
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
                })
                .buildValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    @Test
    @DisplayName("FOOTBALL + MIDFIELDER → valid (no violations)")
    void footballMidfielderValid() {
        RegisterRequest req = new RegisterRequest(
                "a@b.com", "password123", "Test User",
                Sport.FOOTBALL, "MIDFIELDER", ExperienceLevel.INTERMEDIATE);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("FOOTBALL + QUARTERBACK → ValidSportPosition violation")
    void footballInvalidPosition() {
        RegisterRequest req = new RegisterRequest(
                "a@b.com", "password123", "Test User",
                Sport.FOOTBALL, "QUARTERBACK", ExperienceLevel.INTERMEDIATE);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .anyMatch(p -> p.equals("position"));
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(m -> m.contains("not valid for sport FOOTBALL"));
    }

    @Test
    @DisplayName("BASKETBALL + CENTER → valid")
    void basketballCenterValid() {
        RegisterRequest req = new RegisterRequest(
                "a@b.com", "password123", "Test User",
                Sport.BASKETBALL, "CENTER", ExperienceLevel.INTERMEDIATE);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("CRICKET + GOALKEEPER (foreign sport) → violation")
    void crossSportInvalid() {
        RegisterRequest req = new RegisterRequest(
                "a@b.com", "password123", "Test User",
                Sport.CRICKET, "GOALKEEPER", ExperienceLevel.INTERMEDIATE);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(m -> m.contains("not valid for sport CRICKET"));
    }

    @Test
    @DisplayName("blank position → @NotBlank fires, ValidSportPosition skipped")
    void blankPositionDelegatesToNotBlank() {
        RegisterRequest req = new RegisterRequest(
                "a@b.com", "password123", "Test User",
                Sport.FOOTBALL, "", ExperienceLevel.INTERMEDIATE);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);

        // Only the @NotBlank report — no spurious ValidSportPosition message.
        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .allMatch(p -> p.equals("position"));
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .noneMatch(m -> m.contains("not valid for sport"));
    }
}

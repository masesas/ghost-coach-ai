package com.playmotech.ghostcoach.auth.dto;

import com.playmotech.ghostcoach.user.UserPosition;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SportPositionValidator implements ConstraintValidator<ValidSportPosition, RegisterRequest> {

    private final UserPosition userPosition;

    public SportPositionValidator(UserPosition userPosition) {
        this.userPosition = userPosition;
    }

    @Override
    public boolean isValid(RegisterRequest request, ConstraintValidatorContext context) {
        // Let @NotNull / @NotBlank report null/blank cases separately.
        if (request == null || request.sport() == null || request.position() == null
                || request.position().isBlank()) {
            return true;
        }
        boolean ok = userPosition.isValid(request.sport(), request.position());
        if (!ok) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Position '%s' is not valid for sport %s"
                                    .formatted(request.position(), request.sport()))
                    .addPropertyNode("position")
                    .addConstraintViolation();
        }
        return ok;
    }
}

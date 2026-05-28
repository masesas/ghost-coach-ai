package com.playmotech.ghostcoach.auth.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level constraint asserting that the {@code position} value is among the
 * whitelisted entries for the chosen {@link com.playmotech.ghostcoach.user.Sport}.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SportPositionValidator.class)
public @interface ValidSportPosition {
    String message() default "position is not valid for the selected sport";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

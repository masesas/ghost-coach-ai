package com.playmotech.ghostcoach.auth.dto;

import com.playmotech.ghostcoach.user.ExperienceLevel;
import com.playmotech.ghostcoach.user.Sport;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@ValidSportPosition
public record RegisterRequest(
        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8, max = 100)
        String password,

        @NotBlank @Size(min = 2, max = 100)
        String fullName,

        @NotNull
        Sport sport,

        @NotBlank @Size(max = 50)
        String position,

        @NotNull
        ExperienceLevel experienceLevel
) {}

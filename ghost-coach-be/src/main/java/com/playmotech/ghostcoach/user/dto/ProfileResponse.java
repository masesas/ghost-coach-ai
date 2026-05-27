package com.playmotech.ghostcoach.user.dto;

import com.playmotech.ghostcoach.user.ExperienceLevel;
import com.playmotech.ghostcoach.user.Sport;
import com.playmotech.ghostcoach.user.User;

import java.time.Instant;

public record ProfileResponse(
        Long id,
        String email,
        String fullName,
        Sport sport,
        String position,
        ExperienceLevel experienceLevel,
        Instant createdAt
) {
    public static ProfileResponse from(User u) {
        return new ProfileResponse(u.getId(), u.getEmail(), u.getFullName(),
                u.getSport(), u.getPosition(), u.getExperienceLevel(), u.getCreatedAt());
    }
}

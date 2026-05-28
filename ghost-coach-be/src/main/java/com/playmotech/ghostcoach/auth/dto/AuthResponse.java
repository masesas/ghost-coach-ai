package com.playmotech.ghostcoach.auth.dto;

import com.playmotech.ghostcoach.user.dto.ProfileResponse;

public record AuthResponse(String token, ProfileResponse user) {}

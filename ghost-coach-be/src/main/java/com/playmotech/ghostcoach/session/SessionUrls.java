package com.playmotech.ghostcoach.session;

/**
 * Centralizes the image URL pattern so DTO mappers don't hardcode the route.
 * Update this one constant if the public path ever changes.
 */
public final class SessionUrls {

    public static final String SESSION_IMAGE_PATTERN = "/api/v1/sessions/%d/image";

    private SessionUrls() {}

    public static String imageUrl(Long sessionId) {
        return SESSION_IMAGE_PATTERN.formatted(sessionId);
    }
}

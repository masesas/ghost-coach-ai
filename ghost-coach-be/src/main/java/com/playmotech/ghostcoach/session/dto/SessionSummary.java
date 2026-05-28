package com.playmotech.ghostcoach.session.dto;

import com.playmotech.ghostcoach.session.CoachingSession;
import com.playmotech.ghostcoach.session.SessionUrls;

import java.math.BigDecimal;
import java.time.Instant;

public record SessionSummary(
        Long id,
        String imageUrl,
        BigDecimal overallScore,
        String priorityFix,
        Instant createdAt
) {
    public static SessionSummary from(CoachingSession s) {
        return new SessionSummary(
                s.getId(),
                SessionUrls.imageUrl(s.getId()),
                s.getOverallScore(),
                s.getPriorityFix(),
                s.getCreatedAt()
        );
    }
}

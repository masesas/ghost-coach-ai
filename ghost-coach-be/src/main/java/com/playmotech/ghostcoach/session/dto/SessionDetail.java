package com.playmotech.ghostcoach.session.dto;

import com.playmotech.ghostcoach.ai.dto.FeedbackReport;
import com.playmotech.ghostcoach.session.CoachingSession;
import com.playmotech.ghostcoach.session.ConfidenceLevel;
import com.playmotech.ghostcoach.session.SessionUrls;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SessionDetail(
        Long id,
        String imageUrl,
        BigDecimal overallScore,
        List<String> strengths,
        List<FeedbackReport.AreaToImprove> areasToImprove,
        String priorityFix,
        String drillSuggestion,
        ConfidenceLevel confidenceLevel,
        Instant createdAt
) {
    public static SessionDetail from(CoachingSession s) {
        return new SessionDetail(
                s.getId(),
                SessionUrls.imageUrl(s.getId()),
                s.getOverallScore(),
                s.getStrengths(),
                s.getAreasToImprove(),
                s.getPriorityFix(),
                s.getDrillSuggestion(),
                s.getConfidenceLevel(),
                s.getCreatedAt()
        );
    }
}

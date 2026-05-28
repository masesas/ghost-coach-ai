package com.playmotech.ghostcoach.ai.dto;

import java.math.BigDecimal;
import java.util.List;

public record FeedbackReport(
        BigDecimal overallScore,
        List<String> strengths,
        List<AreaToImprove> areasToImprove,
        String priorityFix,
        String drillSuggestion,
        String confidenceLevel,
        QualityCheck qualityCheck
) {
    public record AreaToImprove(String flaw, String explanation) {}

    public record QualityCheck(boolean sufficient, String reason, String detail) {}
}

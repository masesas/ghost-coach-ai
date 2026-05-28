package com.playmotech.ghostcoach.support;

import com.playmotech.ghostcoach.ai.dto.FeedbackReport;
import com.playmotech.ghostcoach.chat.ChatMessage;
import com.playmotech.ghostcoach.chat.ChatRole;
import com.playmotech.ghostcoach.session.CoachingSession;
import com.playmotech.ghostcoach.session.ConfidenceLevel;
import com.playmotech.ghostcoach.user.ExperienceLevel;
import com.playmotech.ghostcoach.user.Sport;
import com.playmotech.ghostcoach.user.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class TestData {

    public static final Instant FIXED_TIME = Instant.parse("2026-01-01T00:00:00Z");

    private TestData() {}

    public static User user() {
        return user(1L, "test@example.com");
    }

    public static User user(Long id, String email) {
        return User.builder()
                .id(id)
                .email(email)
                .passwordHash("$2a$10$dummyhashfortest")
                .fullName("Test User")
                .sport(Sport.FOOTBALL)
                .position("MIDFIELDER")
                .experienceLevel(ExperienceLevel.INTERMEDIATE)
                .createdAt(FIXED_TIME)
                .build();
    }

    public static CoachingSession session(User user) {
        return session(user, 100L);
    }

    public static CoachingSession session(User user, Long sessionId) {
        return CoachingSession.builder()
                .id(sessionId)
                .user(user)
                .imagePath(user.getId() + "/test.jpg")
                .overallScore(BigDecimal.valueOf(8.5))
                .strengths(List.of("good stance", "balanced grip"))
                .areasToImprove(List.of(
                        new FeedbackReport.AreaToImprove("hip rotation",
                                "rotate hips more during follow-through")))
                .priorityFix("Rotate hips more")
                .drillSuggestion("Mirror drill 10x")
                .confidenceLevel(ConfidenceLevel.HIGH)
                .rawAiResponse(validFeedbackJson())
                .createdAt(FIXED_TIME)
                .build();
    }

    public static ChatMessage chatMessage(CoachingSession session, ChatRole role, String content) {
        return ChatMessage.builder()
                .id(System.nanoTime())
                .session(session)
                .role(role)
                .content(content)
                .createdAt(FIXED_TIME)
                .build();
    }

    public static FeedbackReport feedbackReport() {
        return feedbackReport(new FeedbackReport.QualityCheck(true, "OK", ""));
    }

    public static FeedbackReport feedbackReport(FeedbackReport.QualityCheck qualityCheck) {
        return new FeedbackReport(
                BigDecimal.valueOf(8.5),
                List.of("good stance", "balanced grip"),
                List.of(new FeedbackReport.AreaToImprove("hip rotation",
                        "rotate hips more during follow-through")),
                "Rotate hips more",
                "Mirror drill 10x",
                "HIGH",
                qualityCheck
        );
    }

    public static FeedbackReport feedbackReportLegacy() {
        return new FeedbackReport(
                BigDecimal.valueOf(8.5),
                List.of("good stance", "balanced grip"),
                List.of(new FeedbackReport.AreaToImprove("hip rotation",
                        "rotate hips more during follow-through")),
                "Rotate hips more",
                "Mirror drill 10x",
                "HIGH",
                null
        );
    }

    public static String validFeedbackJson() {
        return """
                {
                  "overallScore": 8.5,
                  "strengths": ["good stance", "balanced grip"],
                  "areasToImprove": [
                    {"flaw": "hip rotation", "explanation": "rotate hips more during follow-through"}
                  ],
                  "priorityFix": "Rotate hips more",
                  "drillSuggestion": "Mirror drill 10x",
                  "confidenceLevel": "HIGH"
                }
                """;
    }
}

package com.playmotech.ghostcoach.session;

import com.playmotech.ghostcoach.ai.FeedbackParser;
import com.playmotech.ghostcoach.ai.GeminiClient;
import com.playmotech.ghostcoach.ai.dto.FeedbackReport;
import com.playmotech.ghostcoach.ai.dto.GeminiRequest;
import com.playmotech.ghostcoach.common.exception.ApiException;
import com.playmotech.ghostcoach.prompt.Prompt;
import com.playmotech.ghostcoach.prompt.PromptKey;
import com.playmotech.ghostcoach.prompt.PromptService;
import com.playmotech.ghostcoach.prompt.PromptTemplateRenderer;
import com.playmotech.ghostcoach.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Orchestrates the Gemini call + response parsing for stance analysis.
 *
 * Deliberately not @Transactional — this component performs network IO and
 * must never be invoked from within a database transaction.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionAnalyzer {

    static final String FALLBACK_INSUFFICIENT_DETAIL =
            "Photo is not clear enough to analyze. Please try again.";

    private final GeminiClient geminiClient;
    private final FeedbackParser feedbackParser;
    private final PromptService promptService;

    public AnalysisResult analyze(User user, String contentType, String base64Image) {
        Prompt prompt = promptService.get(PromptKey.STANCE_ANALYSIS);
        String rendered = PromptTemplateRenderer.render(prompt, Map.of(
                "sport", user.getSport().name(),
                "sportLower", user.getSport().name().toLowerCase(),
                "fullName", user.getFullName(),
                "position", user.getPosition(),
                "experienceLevel", user.getExperienceLevel().name(),
                "experienceLevelLower", user.getExperienceLevel().name().toLowerCase()
        ));

        GeminiRequest request = GeminiRequest.of(rendered, contentType, base64Image);
        String rawResponse = geminiClient.generate(request, prompt.getModelConfig(), prompt.getResponseFormat());

        FeedbackReport report = feedbackParser.parse(rawResponse)
                .orElseThrow(() -> ApiException.badGateway("AI_PARSE_FAILED",
                        "Could not parse AI response. Please try again."));

        rejectIfInsufficientQuality(user, report);

        return new AnalysisResult(report, rawResponse);
    }

    private void rejectIfInsufficientQuality(User user, FeedbackReport report) {
        FeedbackReport.QualityCheck qc = report.qualityCheck();
        if (qc == null || qc.sufficient()) {
            return;
        }
        String detail = (qc.detail() == null || qc.detail().isBlank())
                ? FALLBACK_INSUFFICIENT_DETAIL
                : qc.detail();
        log.info("Session rejected for image quality: userId={}, reason={}",
                user.getId(), qc.reason());
        throw ApiException.unprocessableEntity("IMAGE_QUALITY_INSUFFICIENT", detail);
    }

    public record AnalysisResult(FeedbackReport report, String rawResponse) {}
}

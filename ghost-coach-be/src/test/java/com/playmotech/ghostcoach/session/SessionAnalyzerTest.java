package com.playmotech.ghostcoach.session;

import com.playmotech.ghostcoach.ai.FeedbackParser;
import com.playmotech.ghostcoach.ai.GeminiClient;
import com.playmotech.ghostcoach.ai.dto.FeedbackReport;
import com.playmotech.ghostcoach.ai.dto.GeminiRequest;
import com.playmotech.ghostcoach.common.exception.ApiException;
import com.playmotech.ghostcoach.prompt.ModelConfig;
import com.playmotech.ghostcoach.prompt.Prompt;
import com.playmotech.ghostcoach.prompt.PromptKey;
import com.playmotech.ghostcoach.prompt.PromptResponseFormat;
import com.playmotech.ghostcoach.prompt.PromptService;
import com.playmotech.ghostcoach.support.TestData;
import com.playmotech.ghostcoach.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionAnalyzerTest {

    @Mock GeminiClient geminiClient;
    @Mock FeedbackParser feedbackParser;
    @Mock PromptService promptService;

    @InjectMocks SessionAnalyzer analyzer;

    private User user;
    private Prompt stancePrompt;
    private ModelConfig stanceModelConfig;

    @BeforeEach
    void setUp() {
        user = TestData.user();
        stanceModelConfig = new ModelConfig("gemini-2.0-flash", 0.4, 1024);
        stancePrompt = Prompt.builder()
                .promptKey(PromptKey.STANCE_ANALYSIS)
                .description("d")
                .template("Coach {{fullName}} sport={{sport}} pos={{position}} lvl={{experienceLevel}} lower={{sportLower}}/{{experienceLevelLower}}")
                .variables(List.of("fullName", "sport", "position", "experienceLevel", "sportLower", "experienceLevelLower"))
                .modelConfig(stanceModelConfig)
                .responseFormat(PromptResponseFormat.JSON)
                .build();
    }

    @Test
    @DisplayName("analyze: happy path renders prompt and forwards model config + JSON format")
    void analyzeHappyPath() {
        when(promptService.get(PromptKey.STANCE_ANALYSIS)).thenReturn(stancePrompt);
        when(geminiClient.generate(any(GeminiRequest.class), eq(stanceModelConfig), eq(PromptResponseFormat.JSON)))
                .thenReturn("ai-raw");
        when(feedbackParser.parse("ai-raw")).thenReturn(Optional.of(TestData.feedbackReport()));

        SessionAnalyzer.AnalysisResult result = analyzer.analyze(user, "image/jpeg", "BASE64");

        assertThat(result.rawResponse()).isEqualTo("ai-raw");
        assertThat(result.report()).isEqualTo(TestData.feedbackReport());

        ArgumentCaptor<GeminiRequest> captor = ArgumentCaptor.forClass(GeminiRequest.class);
        verify(geminiClient).generate(captor.capture(), eq(stanceModelConfig), eq(PromptResponseFormat.JSON));
        String renderedPrompt = captor.getValue().contents().get(0).parts().get(0).text();
        assertThat(renderedPrompt)
                .contains("Test User")
                .contains("FOOTBALL")
                .contains("MIDFIELDER")
                .contains("INTERMEDIATE")
                .contains("football")
                .contains("intermediate");
    }

    @Test
    @DisplayName("analyze: parser empty → ApiException 502 AI_PARSE_FAILED")
    void analyzeParseFails() {
        when(promptService.get(PromptKey.STANCE_ANALYSIS)).thenReturn(stancePrompt);
        when(geminiClient.generate(any(GeminiRequest.class), any(), any())).thenReturn("garbage");
        when(feedbackParser.parse("garbage")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> analyzer.analyze(user, "image/jpeg", "BASE64"))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(ex.getCode()).isEqualTo("AI_PARSE_FAILED");
                });
    }

    @Test
    @DisplayName("analyze: Gemini exception propagates without calling parser")
    void analyzeGeminiThrowsPropagates() {
        when(promptService.get(PromptKey.STANCE_ANALYSIS)).thenReturn(stancePrompt);
        when(geminiClient.generate(any(GeminiRequest.class), any(), any()))
                .thenThrow(ApiException.badGateway("AI_ERROR", "gemini down"));

        assertThatThrownBy(() -> analyzer.analyze(user, "image/jpeg", "BASE64"))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo("AI_ERROR");
                });
    }

    @Test
    @DisplayName("analyze: qualityCheck sufficient=false → throws 422 IMAGE_QUALITY_INSUFFICIENT with detail")
    void analyzeInsufficientQualityRejects() {
        when(promptService.get(PromptKey.STANCE_ANALYSIS)).thenReturn(stancePrompt);
        when(geminiClient.generate(any(GeminiRequest.class), any(), any())).thenReturn("raw");
        FeedbackReport report = TestData.feedbackReport(
                new FeedbackReport.QualityCheck(false, "BLURRY", "Photo is too blurry. Retake."));
        when(feedbackParser.parse("raw")).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> analyzer.analyze(user, "image/jpeg", "BASE64"))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(ex.getCode()).isEqualTo("IMAGE_QUALITY_INSUFFICIENT");
                    assertThat(ex.getMessage()).isEqualTo("Photo is too blurry. Retake.");
                });
    }

    @Test
    @DisplayName("analyze: qualityCheck sufficient=false with blank detail → fallback message")
    void analyzeInsufficientQualityFallbackMessage() {
        when(promptService.get(PromptKey.STANCE_ANALYSIS)).thenReturn(stancePrompt);
        when(geminiClient.generate(any(GeminiRequest.class), any(), any())).thenReturn("raw");
        FeedbackReport report = TestData.feedbackReport(
                new FeedbackReport.QualityCheck(false, "NO_PERSON", "  "));
        when(feedbackParser.parse("raw")).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> analyzer.analyze(user, "image/jpeg", "BASE64"))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo("IMAGE_QUALITY_INSUFFICIENT");
                    assertThat(ex.getMessage()).isEqualTo(SessionAnalyzer.FALLBACK_INSUFFICIENT_DETAIL);
                });
    }

    @Test
    @DisplayName("analyze: qualityCheck sufficient=false with null detail → fallback message")
    void analyzeInsufficientQualityNullDetailFallback() {
        when(promptService.get(PromptKey.STANCE_ANALYSIS)).thenReturn(stancePrompt);
        when(geminiClient.generate(any(GeminiRequest.class), any(), any())).thenReturn("raw");
        FeedbackReport report = TestData.feedbackReport(
                new FeedbackReport.QualityCheck(false, "OTHER", null));
        when(feedbackParser.parse("raw")).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> analyzer.analyze(user, "image/jpeg", "BASE64"))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getMessage()).isEqualTo(SessionAnalyzer.FALLBACK_INSUFFICIENT_DETAIL);
                });
    }

    @Test
    @DisplayName("analyze: legacy response (qualityCheck=null) → gate skipped, returns result")
    void analyzeLegacyQualityCheckNull() {
        when(promptService.get(PromptKey.STANCE_ANALYSIS)).thenReturn(stancePrompt);
        when(geminiClient.generate(any(GeminiRequest.class), any(), any())).thenReturn("raw");
        when(feedbackParser.parse("raw")).thenReturn(Optional.of(TestData.feedbackReportLegacy()));

        SessionAnalyzer.AnalysisResult result = analyzer.analyze(user, "image/jpeg", "BASE64");

        assertThat(result.rawResponse()).isEqualTo("raw");
        assertThat(result.report().qualityCheck()).isNull();
    }
}

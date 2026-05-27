package com.playmotech.ghostcoach.ai;

import com.playmotech.ghostcoach.ai.dto.GeminiRequest;
import com.playmotech.ghostcoach.ai.dto.GeminiResponse;
import com.playmotech.ghostcoach.common.exception.ApiException;
import com.playmotech.ghostcoach.config.AppConfigProp;
import com.playmotech.ghostcoach.prompt.ModelConfig;
import com.playmotech.ghostcoach.prompt.PromptResponseFormat;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiClient {

    private static final String API_KEY_HEADER = "x-goog-api-key";
    private static final String JSON_MIME_TYPE = "application/json";

    private final RestClient restClient;
    private final AppConfigProp configProp;

    @PostConstruct
    void validateConfig() {
        Objects.requireNonNull(configProp.getGemini(), "app.gemini config required");
        Objects.requireNonNull(configProp.getGemini().getApiKey(), "app.gemini.api-key required");
        Objects.requireNonNull(configProp.getGemini().getBaseUrl(), "app.gemini.base-url required");
        Objects.requireNonNull(configProp.getGemini().getModel(), "app.gemini.model required");
    }

    public String generate(GeminiRequest request, ModelConfig override, PromptResponseFormat format) {
        String model = resolveModel(override);
        GeminiRequest effective = applyGenerationConfig(request, override, format);
        String url = "%s/models/%s:generateContent".formatted(
                configProp.getGemini().getBaseUrl(),
                model
        );

        try {
            GeminiResponse response = restClient.post()
                    .uri(url)
                    .header(API_KEY_HEADER, configProp.getGemini().getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(effective)
                    .retrieve()
                    .body(GeminiResponse.class);

            if (response == null) {
                throw ApiException.badGateway("AI_NO_RESPONSE", "Gemini returned empty response");
            }
            String text = response.extractText();
            if (text.isBlank()) {
                throw ApiException.badGateway("AI_NO_RESPONSE", "Gemini returned empty text");
            }
            return text;
        } catch (RestClientException e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw ApiException.badGateway("AI_ERROR", "Failed to get response from AI service");
        }
    }

    private String resolveModel(ModelConfig override) {
        if (override != null && override.model() != null && !override.model().isBlank()) {
            return override.model();
        }
        return configProp.getGemini().getModel();
    }

    private GeminiRequest applyGenerationConfig(GeminiRequest request,
                                                ModelConfig override,
                                                PromptResponseFormat format) {
        Double temperature = override != null ? override.temperature() : null;
        Integer maxOutputTokens = override != null ? override.maxOutputTokens() : null;
        String responseMimeType = format == PromptResponseFormat.JSON ? JSON_MIME_TYPE : null;

        if (temperature == null && maxOutputTokens == null && responseMimeType == null) {
            return request;
        }

        return request.withGenerationConfig(
                new GeminiRequest.GenerationConfig(temperature, maxOutputTokens, responseMimeType)
        );
    }
}

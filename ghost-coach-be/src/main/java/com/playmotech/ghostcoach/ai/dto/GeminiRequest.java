package com.playmotech.ghostcoach.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiRequest(
        List<Content> contents,
        GenerationConfig generationConfig
) {

    public record Content(List<Part> parts) {}

    public record Part(String text, InlineData inlineData) {

        public static Part text(String text) {
            return new Part(text, null);
        }

        public static Part image(String mimeType, String base64Data) {
            return new Part(null, new InlineData(mimeType, base64Data));
        }
    }

    public record InlineData(String mimeType, String data) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GenerationConfig(
            Double temperature,
            Integer maxOutputTokens,
            String responseMimeType
    ) {}

    public static GeminiRequest of(String prompt, String mimeType, String base64Image) {
        return new GeminiRequest(
                List.of(new Content(List.of(Part.text(prompt), Part.image(mimeType, base64Image)))),
                null
        );
    }

    public static GeminiRequest textOnly(String prompt) {
        return new GeminiRequest(
                List.of(new Content(List.of(Part.text(prompt)))),
                null
        );
    }

    public GeminiRequest withGenerationConfig(GenerationConfig config) {
        return new GeminiRequest(contents, config);
    }
}

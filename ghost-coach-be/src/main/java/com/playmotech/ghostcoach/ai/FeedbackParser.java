package com.playmotech.ghostcoach.ai;

import com.playmotech.ghostcoach.ai.dto.FeedbackReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedbackParser {

    private static final int LOG_TRUNCATE = 200;

    private final ObjectMapper objectMapper;

    public Optional<FeedbackReport> parse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return Optional.empty();
        }

        String json = extractJson(rawResponse);
        if (json == null) {
            log.warn("No JSON object found in AI response: {}", truncate(rawResponse));
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(json, FeedbackReport.class));
        } catch (JacksonException e) {
            log.warn("Failed to parse AI response as FeedbackReport: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Locates the first balanced JSON object in {@code raw}. Handles:
     * <ul>
     *   <li>plain JSON, with or without surrounding whitespace</li>
     *   <li>fenced markdown (``` or ```json)</li>
     *   <li>JSON embedded inside narrative text</li>
     *   <li>nested objects + braces that appear inside string literals</li>
     * </ul>
     * Returns {@code null} when no balanced object can be found.
     */
    private String extractJson(String raw) {
        String s = stripCodeFence(raw.trim());

        int start = s.indexOf('{');
        if (start < 0) return null;

        int end = findMatchingBrace(s, start);
        if (end < 0) return null;

        return s.substring(start, end + 1);
    }

    private String stripCodeFence(String s) {
        if (!s.startsWith("```")) return s;
        int nl = s.indexOf('\n');
        if (nl < 0) return s;
        String body = s.substring(nl + 1);
        if (body.endsWith("```")) {
            body = body.substring(0, body.length() - 3);
        }
        return body.trim();
    }

    private int findMatchingBrace(String s, int start) {
        int depth = 0;
        boolean inString = false;
        boolean escape = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) continue;
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static String truncate(String s) {
        return s.length() <= LOG_TRUNCATE ? s : s.substring(0, LOG_TRUNCATE) + "…";
    }
}

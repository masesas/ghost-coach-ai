package com.playmotech.ghostcoach.ai.dto;

import java.util.List;

public record GeminiResponse(List<Candidate> candidates) {

    public record Candidate(Content content) {}

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    public String extractText() {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }
        Candidate c = candidates.getFirst();
        if (c.content() == null || c.content().parts() == null || c.content().parts().isEmpty()) {
            return "";
        }
        return c.content().parts().getFirst().text();
    }
}

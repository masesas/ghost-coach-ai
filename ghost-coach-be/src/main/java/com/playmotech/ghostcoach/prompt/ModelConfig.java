package com.playmotech.ghostcoach.prompt;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ModelConfig(
        String model,
        Double temperature,
        Integer maxOutputTokens
) {
}

package com.playmotech.ghostcoach.prompt;

import com.playmotech.ghostcoach.prompt.exception.PromptVariableMissingException;
import com.playmotech.ghostcoach.prompt.exception.PromptVariableUnknownException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromptTemplateRendererTest {

    @Test
    @DisplayName("substitutes single placeholder")
    void render_singlePlaceholder_substitutes() {
        Prompt prompt = promptOf(
                "Hello {{name}}",
                List.of("name")
        );

        String result = PromptTemplateRenderer.render(prompt, Map.of("name", "Ana"));

        assertThat(result).isEqualTo("Hello Ana");
    }

    @Test
    @DisplayName("substitutes multiple placeholders including repeated ones")
    void render_multiplePlaceholders_substitutesAll() {
        Prompt prompt = promptOf(
                "Sport: {{sport}}. Player: {{name}}. Sport again: {{sport}}.",
                List.of("sport", "name")
        );

        String result = PromptTemplateRenderer.render(prompt, Map.of(
                "sport", "BASKETBALL",
                "name", "Budi"
        ));

        assertThat(result).isEqualTo("Sport: BASKETBALL. Player: Budi. Sport again: BASKETBALL.");
    }

    @Test
    @DisplayName("returns template unchanged when no placeholder present")
    void render_noPlaceholder_returnsAsIs() {
        Prompt prompt = promptOf("Plain text with no vars", List.of());

        String result = PromptTemplateRenderer.render(prompt, Map.of());

        assertThat(result).isEqualTo("Plain text with no vars");
    }

    @Test
    @DisplayName("escapes regex special characters in replacement value")
    void render_valueWithRegexSpecialChars_escapesProperly() {
        Prompt prompt = promptOf("Path: {{p}}", List.of("p"));

        String result = PromptTemplateRenderer.render(prompt, Map.of("p", "$1 \\ backslash"));

        assertThat(result).isEqualTo("Path: $1 \\ backslash");
    }

    @Test
    @DisplayName("converts non-string values via toString")
    void render_nonStringValue_callsToString() {
        Prompt prompt = promptOf("Score: {{score}}", List.of("score"));

        String result = PromptTemplateRenderer.render(prompt, Map.of("score", 42));

        assertThat(result).isEqualTo("Score: 42");
    }

    @Test
    @DisplayName("throws when placeholder value is null")
    void render_placeholderValueNull_throws() {
        Prompt prompt = promptOf("Hello {{name}}", List.of("name"));
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", null);

        assertThatThrownBy(() -> PromptTemplateRenderer.render(prompt, vars))
                .isInstanceOf(PromptVariableMissingException.class)
                .hasMessageContaining("name");
    }

    @Test
    @DisplayName("throws when placeholder key is not provided")
    void render_placeholderKeyAbsent_throws() {
        Prompt prompt = promptOf("Hello {{name}}", List.of("name"));

        assertThatThrownBy(() -> PromptTemplateRenderer.render(prompt, Map.of()))
                .isInstanceOf(PromptVariableMissingException.class)
                .hasMessageContaining("name");
    }

    @Test
    @DisplayName("throws when caller passes variable not in whitelist")
    void render_unknownVarPassed_throws() {
        Prompt prompt = promptOf("Hello {{name}}", List.of("name"));

        assertThatThrownBy(() -> PromptTemplateRenderer.render(prompt, Map.of(
                "name", "Ana",
                "extra", "should-fail"
        )))
                .isInstanceOf(PromptVariableUnknownException.class)
                .hasMessageContaining("extra");
    }

    @Test
    @DisplayName("does NOT match malformed placeholders")
    void render_malformedPlaceholder_leftAsIs() {
        Prompt prompt = promptOf("Single {brace} and {{good}}", List.of("good"));

        String result = PromptTemplateRenderer.render(prompt, Map.of("good", "OK"));

        assertThat(result).isEqualTo("Single {brace} and OK");
    }

    private static Prompt promptOf(String template, List<String> variables) {
        return Prompt.builder()
                .promptKey(PromptKey.STANCE_ANALYSIS)
                .description("test")
                .template(template)
                .variables(variables)
                .modelConfig(new ModelConfig("gemini-2.0-flash", 0.4, 1024))
                .responseFormat(PromptResponseFormat.TEXT)
                .build();
    }
}

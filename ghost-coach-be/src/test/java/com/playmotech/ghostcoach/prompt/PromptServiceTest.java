package com.playmotech.ghostcoach.prompt;

import com.playmotech.ghostcoach.prompt.exception.PromptNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptServiceTest {

    @Mock
    PromptRepository repository;

    @InjectMocks
    PromptService service;

    private Prompt stancePrompt;

    @BeforeEach
    void setUp() {
        stancePrompt = Prompt.builder()
                .id(1L)
                .promptKey(PromptKey.STANCE_ANALYSIS)
                .description("desc")
                .template("hello {{name}}")
                .variables(List.of("name"))
                .modelConfig(new ModelConfig("gemini-2.0-flash", 0.4, 1024))
                .responseFormat(PromptResponseFormat.JSON)
                .build();
    }

    @Test
    @DisplayName("get returns prompt when present in repository")
    void get_present_returnsPrompt() {
        when(repository.findByPromptKey(PromptKey.STANCE_ANALYSIS))
                .thenReturn(Optional.of(stancePrompt));

        Prompt result = service.get(PromptKey.STANCE_ANALYSIS);

        assertThat(result).isSameAs(stancePrompt);
        verify(repository).findByPromptKey(PromptKey.STANCE_ANALYSIS);
    }

    @Test
    @DisplayName("get throws PromptNotFoundException when absent")
    void get_absent_throws() {
        when(repository.findByPromptKey(PromptKey.CHAT_COACHING))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(PromptKey.CHAT_COACHING))
                .isInstanceOf(PromptNotFoundException.class)
                .hasMessageContaining("CHAT_COACHING");
    }
}

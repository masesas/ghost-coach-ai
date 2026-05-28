package com.playmotech.ghostcoach.prompt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PromptCacheWarmerTest {

    @Mock PromptService promptService;

    @InjectMocks PromptCacheWarmer warmer;

    @Test
    @DisplayName("evictOnStartup invokes evictAll on PromptService")
    void evictOnStartup_callsEvictAll() {
        warmer.evictOnStartup();
        verify(promptService).evictAll();
    }
}

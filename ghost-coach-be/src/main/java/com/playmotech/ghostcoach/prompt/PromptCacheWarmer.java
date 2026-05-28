package com.playmotech.ghostcoach.prompt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Evicts the Caffeine prompt cache on application startup.
 *
 * <p>After a Flyway migration that modifies a prompt template (e.g. V7 adding the
 * {@code chatHistory} variable), an instance restarted into the new code with a
 * stale cache entry would still serve the previous template and cause
 * {@link com.playmotech.ghostcoach.prompt.exception.PromptVariableUnknownException}
 * when {@code buildVars()} includes new keys.
 *
 * <p>Evicting at startup guarantees the next {@link PromptService#get} reads the
 * latest row from the database. The cache repopulates lazily.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PromptCacheWarmer {

    private final PromptService promptService;

    @EventListener(ApplicationReadyEvent.class)
    public void evictOnStartup() {
        promptService.evictAll();
        log.info("Prompt cache evicted on startup");
    }
}

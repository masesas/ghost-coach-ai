package com.playmotech.ghostcoach.prompt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {PromptService.class, PromptServiceCacheTest.CacheTestConfig.class})
@Import(PromptServiceCacheTest.CacheTestConfig.class)
@ActiveProfiles("test")
@DisplayName("PromptService @Cacheable behavior")
class PromptServiceCacheTest {

    @TestConfiguration
    @EnableCaching
    static class CacheTestConfig {
        @Bean
        CacheManager cacheManager() {
            return new CaffeineCacheManager(PromptService.CACHE_NAME);
        }
    }

    @MockitoBean
    PromptRepository repository;

    @Autowired
    PromptService service;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    @Test
    @DisplayName("second call hits cache and skips repository")
    void get_secondCall_servesFromCache() {
        Prompt stub = Prompt.builder()
                .id(1L)
                .promptKey(PromptKey.STANCE_ANALYSIS)
                .description("d")
                .template("t")
                .variables(List.of())
                .modelConfig(new ModelConfig("m", 0.1, 100))
                .responseFormat(PromptResponseFormat.TEXT)
                .build();
        when(repository.findByPromptKey(PromptKey.STANCE_ANALYSIS))
                .thenReturn(Optional.of(stub));

        Prompt first = service.get(PromptKey.STANCE_ANALYSIS);
        Prompt second = service.get(PromptKey.STANCE_ANALYSIS);

        assertThat(first).isSameAs(second);
        verify(repository, times(1)).findByPromptKey(PromptKey.STANCE_ANALYSIS);
    }

    @Test
    @DisplayName("evict clears cached entry and causes repo reload on next call")
    void evict_singleKey_reloadsFromRepo() {
        Prompt stub = Prompt.builder()
                .id(1L)
                .promptKey(PromptKey.CHAT_COACHING)
                .description("d")
                .template("t")
                .variables(List.of())
                .modelConfig(new ModelConfig("m", 0.1, 100))
                .responseFormat(PromptResponseFormat.TEXT)
                .build();
        when(repository.findByPromptKey(PromptKey.CHAT_COACHING))
                .thenReturn(Optional.of(stub));

        service.get(PromptKey.CHAT_COACHING);
        service.evict(PromptKey.CHAT_COACHING);
        service.get(PromptKey.CHAT_COACHING);

        verify(repository, times(2)).findByPromptKey(PromptKey.CHAT_COACHING);
    }
}

package com.playmotech.ghostcoach.prompt;

import com.playmotech.ghostcoach.prompt.exception.PromptNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PromptService {

    public static final String CACHE_NAME = "prompts";

    private final PromptRepository repository;

    public PromptService(PromptRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = CACHE_NAME, key = "#key.name()")
    public Prompt get(PromptKey key) {
        return repository.findByPromptKey(key)
                .orElseThrow(() -> new PromptNotFoundException(key));
    }

    @CacheEvict(value = CACHE_NAME, key = "#key.name()")
    public void evict(PromptKey key) {
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void evictAll() {
    }
}

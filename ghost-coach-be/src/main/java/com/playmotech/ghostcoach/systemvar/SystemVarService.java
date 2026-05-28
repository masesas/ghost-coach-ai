package com.playmotech.ghostcoach.systemvar;

import com.playmotech.ghostcoach.systemvar.dto.SystemVarItem;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SystemVarService {

    public static final String CACHE_NAME = "system-vars";

    private final SystemVarRepository repository;

    @Cacheable(value = CACHE_NAME, key = "#groupCode")
    @Transactional(readOnly = true)
    public List<SystemVarItem> listByGroup(String groupCode) {
        return repository
                .findByGroupCodeAndActiveTrueOrderBySortOrderAscIdAsc(groupCode)
                .stream()
                .map(SystemVarItem::from)
                .toList();
    }

    public List<SystemVarItem> list(String category, String scope) {
        return listByGroup(composeGroupCode(category, scope));
    }

    public boolean isValidKey(String category, String scope, String key) {
        if (key == null) return false;
        return list(category, scope).stream().anyMatch(it -> it.key().equals(key));
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void evictAll() {
    }

    static String composeGroupCode(String category, String scope) {
        String c = Objects.requireNonNull(category, "category required").trim().toUpperCase();
        if (c.isBlank()) {
            throw new IllegalArgumentException("category must be non-blank");
        }
        if (scope == null || scope.isBlank()) return c;
        return c + "_" + scope.trim().toUpperCase();
    }
}

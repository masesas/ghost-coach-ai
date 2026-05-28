package com.playmotech.ghostcoach.systemvar;

import com.playmotech.ghostcoach.systemvar.dto.SystemVarItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {SystemVarService.class, SystemVarServiceCacheTest.CacheTestConfig.class})
@Import(SystemVarServiceCacheTest.CacheTestConfig.class)
@ActiveProfiles("test")
@DisplayName("SystemVarService @Cacheable behavior")
class SystemVarServiceCacheTest {

    @TestConfiguration
    @EnableCaching
    static class CacheTestConfig {
        @Bean
        CacheManager cacheManager() {
            return new CaffeineCacheManager(SystemVarService.CACHE_NAME);
        }
    }

    @MockitoBean
    SystemVarRepository repository;

    @Autowired
    SystemVarService service;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    private static SystemVar entity(String group, String key, String label) {
        return SystemVar.builder()
                .id(1L)
                .groupCode(group)
                .itemKey(key)
                .label(label)
                .sortOrder(1)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("second listByGroup hits cache and skips repository")
    void listByGroup_secondCall_servesFromCache() {
        when(repository.findByGroupCodeAndActiveTrueOrderBySortOrderAscIdAsc("POSITION_FOOTBALL"))
                .thenReturn(List.of(entity("POSITION_FOOTBALL", "GOALKEEPER", "Goalkeeper")));

        List<SystemVarItem> first = service.listByGroup("POSITION_FOOTBALL");
        List<SystemVarItem> second = service.listByGroup("POSITION_FOOTBALL");

        assertThat(first).isSameAs(second);
        verify(repository, times(1))
                .findByGroupCodeAndActiveTrueOrderBySortOrderAscIdAsc("POSITION_FOOTBALL");
    }

    @Test
    @DisplayName("evictAll clears entries and forces reload on next call")
    void evictAll_clearsAllEntries() {
        when(repository.findByGroupCodeAndActiveTrueOrderBySortOrderAscIdAsc("POSITION_CRICKET"))
                .thenReturn(List.of(entity("POSITION_CRICKET", "BATSMAN", "Batsman")));

        service.listByGroup("POSITION_CRICKET");
        service.evictAll();
        service.listByGroup("POSITION_CRICKET");

        verify(repository, times(2))
                .findByGroupCodeAndActiveTrueOrderBySortOrderAscIdAsc("POSITION_CRICKET");
    }
}

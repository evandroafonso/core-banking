package com.tuum.corebanking.config;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    private CacheConfig config;

    @BeforeEach
    void setUp() {
        config = new CacheConfig();
    }

    @Test
    void cacheManagerShouldBeCaffeineCacheManager() {
        CacheManager cacheManager = config.cacheManager();

        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
    }

    @Test
    void cacheManagerShouldExposeAccountIdsCache() {
        CacheManager cacheManager = config.cacheManager();

        Cache cache = cacheManager.getCache("accountIds");

        assertThat(cache).isNotNull();
        assertThat(cache.getName()).isEqualTo("accountIds");
    }

    @Test
    void cacheManagerShouldNotExposeUndeclaredCache() {
        CacheManager cacheManager = config.cacheManager();

        Cache cache = cacheManager.getCache("nonExistentCache");

        assertThat(cache).isNull();
    }

    @Test
    void cacheManagerShouldConfigureStatsRecording() {
        CacheManager cacheManager = config.cacheManager();

        CaffeineCache cache = (CaffeineCache) cacheManager.getCache("accountIds");
        CacheStats stats = cache.getNativeCache().stats();

        assertThat(stats).isNotNull();
    }

    @Test
    void cacheManagerShouldAllowPutAndGetOperations() {
        CacheManager cacheManager = config.cacheManager();
        Cache cache = cacheManager.getCache("accountIds");

        cache.put("account-1", "value-1");

        assertThat(cache.get("account-1").get()).isEqualTo("value-1");
    }
}
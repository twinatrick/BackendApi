package com.example.BackendApi.Config;

import com.example.BackendApi.Service.IBloomFilterService;
import com.example.BackendApi.Util.NullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CachePenetrationProtectionCacheTest {

    @Mock
    private RedisCache delegate;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Mock
    private IBloomFilterService bloomFilterService;

    @Mock
    private RedissonClient redissonClient;

    private CachePenetrationProtectionCache cache;

    private final String cacheName = "test-cache";
    private final String testKey = "test-key";
    private final String nullKey = "null:test-cache:test-key";
    private final Duration nullTtl = Duration.ofMinutes(5);

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        cache = new CachePenetrationProtectionCache(
                cacheName, delegate, stringRedisTemplate,
                bloomFilterService, redissonClient, nullTtl);
    }

    @Test
    void get_WhenNullMarkerExists_ReturnsNull() {
        when(stringRedisTemplate.hasKey(nullKey)).thenReturn(true);

        Cache.ValueWrapper result = cache.get(testKey);

        assertNotNull(result);
        assertNull(result.get());
        verify(delegate, never()).get(any());
    }

    @Test
    void get_WhenCacheMiss_ReturnsNull() {
        when(stringRedisTemplate.hasKey(nullKey)).thenReturn(false);

        Cache.ValueWrapper result = cache.get(testKey);

        assertNull(result);
        verify(delegate).get(testKey);
    }

    @Test
    void get_WhenDelegateReturnsNullValue_ReturnsNull() {
        when(stringRedisTemplate.hasKey(nullKey)).thenReturn(false);
        when(delegate.get(testKey)).thenReturn(() -> NullValue.INSTANCE);

        Cache.ValueWrapper result = cache.get(testKey);

        assertNotNull(result);
        assertNull(result.get());
    }

    @Test
    void get_WhenDelegateReturnsActualValue_ReturnsValue() {
        String expectedValue = "real-value";
        when(stringRedisTemplate.hasKey(nullKey)).thenReturn(false);
        when(delegate.get(testKey)).thenReturn(() -> expectedValue);

        Cache.ValueWrapper result = cache.get(testKey);

        assertNotNull(result);
        assertEquals(expectedValue, result.get());
    }

    @Test
    void put_WithNullValue_StoresNullMarker() {
        cache.put(testKey, null);

        verify(valueOps).set(nullKey, "", nullTtl);
        verify(delegate, never()).put(any(), any());
        verify(bloomFilterService, never()).add(anyString(), anyString());
    }

    @Test
    void put_WithActualValue_AddsToBloomFilterAndDelegate() {
        String value = "real-value";

        cache.put(testKey, value);

        verify(bloomFilterService).add(cacheName, testKey);
        verify(delegate).put(testKey, value);
        verify(valueOps, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void evict_ClearsNullMarkerAndDelegate() {
        cache.evict(testKey);

        verify(stringRedisTemplate).delete(nullKey);
        verify(delegate).evict(testKey);
    }

    @Test
    void getName_ReturnsCorrectName() {
        assertEquals(cacheName, cache.getName());
    }
}

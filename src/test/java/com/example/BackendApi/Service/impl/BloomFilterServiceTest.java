package com.example.BackendApi.Service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BloomFilterServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RBloomFilter<Object> bloomFilter;

    @Test
    void mightContain_WhenFilterSaysYes_ReturnsTrue() {
        when(redissonClient.getBloomFilter(anyString())).thenReturn(bloomFilter);
        when(bloomFilter.isExists()).thenReturn(true);
        when(bloomFilter.contains("existing-key")).thenReturn(true);

        BloomFilterService service = new BloomFilterService(redissonClient);
        assertTrue(service.mightContain("test-cache", "existing-key"));
    }

    @Test
    void mightContain_WhenFilterSaysNo_ReturnsFalse() {
        when(redissonClient.getBloomFilter(anyString())).thenReturn(bloomFilter);
        when(bloomFilter.isExists()).thenReturn(true);
        when(bloomFilter.contains("unknown-key")).thenReturn(false);

        BloomFilterService service = new BloomFilterService(redissonClient);
        assertFalse(service.mightContain("test-cache", "unknown-key"));
    }

    @Test
    void add_DelegatesToFilter() {
        when(redissonClient.getBloomFilter(anyString())).thenReturn(bloomFilter);
        when(bloomFilter.isExists()).thenReturn(true);

        BloomFilterService service = new BloomFilterService(redissonClient);
        service.add("test-cache", "new-key");

        verify(bloomFilter).add("new-key");
    }

    @Test
    void addAll_DelegatesToFilter() {
        when(redissonClient.getBloomFilter(anyString())).thenReturn(bloomFilter);
        when(bloomFilter.isExists()).thenReturn(true);

        BloomFilterService service = new BloomFilterService(redissonClient);
        service.addAll("test-cache", java.util.List.of("key1", "key2", "key3"));

        verify(bloomFilter).add("key1");
        verify(bloomFilter).add("key2");
        verify(bloomFilter).add("key3");
    }

    @Test
    void getOrCreate_InitializesNewFilter() {
        when(redissonClient.getBloomFilter("bloom:new-cache")).thenReturn(bloomFilter);
        when(bloomFilter.isExists()).thenReturn(false);

        BloomFilterService service = new BloomFilterService(redissonClient);
        service.mightContain("new-cache", "key");

        verify(bloomFilter).tryInit(10000L, 0.01);
    }

    @Test
    void getOrCreate_ReusesExistingFilter() {
        when(redissonClient.getBloomFilter("bloom:existing-cache")).thenReturn(bloomFilter);
        when(bloomFilter.isExists()).thenReturn(true);

        BloomFilterService service = new BloomFilterService(redissonClient);
        service.mightContain("existing-cache", "key1");
        service.mightContain("existing-cache", "key2");

        verify(bloomFilter, never()).tryInit(anyLong(), anyDouble());
    }
}

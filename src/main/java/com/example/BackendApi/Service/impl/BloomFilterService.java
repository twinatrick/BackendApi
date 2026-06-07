package com.example.BackendApi.Service.impl;

import com.example.BackendApi.Service.IBloomFilterService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class BloomFilterService implements IBloomFilterService {

    private static final double DEFAULT_FALSE_PROBABILITY = 0.01;

    private static final long DEFAULT_EXPECTED_INSERTIONS = 10000L;

    private final RedissonClient redissonClient;

    private final Map<String, RBloomFilter<String>> filters = new ConcurrentHashMap<>();

    public BloomFilterService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public boolean mightContain(String cacheName, String key) {
        RBloomFilter<String> filter = getOrCreate(cacheName);
        return filter.contains(key);
    }

    public void add(String cacheName, String key) {
        RBloomFilter<String> filter = getOrCreate(cacheName);
        filter.add(key);
    }

    public void addAll(String cacheName, Collection<String> keys) {
        RBloomFilter<String> filter = getOrCreate(cacheName);
        for (String key : keys) {
            filter.add(key);
        }
    }

    public long count(String cacheName) {
        RBloomFilter<String> filter = getOrCreate(cacheName);
        return filter.count();
    }

    public void clear(String cacheName) {
        RBloomFilter<String> filter = filters.remove(cacheName);
        if (filter != null) {
            filter.delete();
        }
    }

    public RBloomFilter<String> getOrCreate(String cacheName) {
        return filters.computeIfAbsent(cacheName, name -> {
            RBloomFilter<String> filter = redissonClient.getBloomFilter("bloom:" + name);
            if (!filter.isExists()) {
                filter.tryInit(DEFAULT_EXPECTED_INSERTIONS, DEFAULT_FALSE_PROBABILITY);
                log.info("初始化布隆過濾器 [bloom:{}] - 預期資料量: {}, 誤判率: {}",
                        name, DEFAULT_EXPECTED_INSERTIONS, DEFAULT_FALSE_PROBABILITY);
            }
            return filter;
        });
    }

    @PreDestroy
    public void destroy() {
        filters.clear();
    }
}

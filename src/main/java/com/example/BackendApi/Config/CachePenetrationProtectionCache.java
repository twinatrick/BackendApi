package com.example.BackendApi.Config;

import com.example.BackendApi.Service.IBloomFilterService;
import com.example.BackendApi.Util.NullValue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class CachePenetrationProtectionCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(CachePenetrationProtectionCache.class);

    private static final long LOCK_WAIT_MILLIS = 200;

    private final String name;

    private final RedisCache delegate;

    private final StringRedisTemplate stringRedisTemplate;

    private final IBloomFilterService bloomFilterService;

    private final RedissonClient redissonClient;

    private final Duration nullValueTtl;

    public CachePenetrationProtectionCache(String name, RedisCache delegate,
                                            StringRedisTemplate stringRedisTemplate,
                                            IBloomFilterService bloomFilterService,
                                            RedissonClient redissonClient,
                                            Duration nullValueTtl) {
        this.name = name;
        this.delegate = delegate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.bloomFilterService = bloomFilterService;
        this.redissonClient = redissonClient;
        this.nullValueTtl = nullValueTtl;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        String cacheKey = toCacheKey(key);

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(nullKey(cacheKey)))) {
            return () -> null;
        }

        ValueWrapper result = delegate.get(key);
        if (result != null && result.get() instanceof NullValue) {
            return () -> null;
        }

        return result;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        String cacheKey = toCacheKey(key);

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(nullKey(cacheKey)))) {
            return null;
        }

        T result = delegate.get(key, type);
        if (result instanceof NullValue) {
            return null;
        }

        return result;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        String cacheKey = toCacheKey(key);

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(nullKey(cacheKey)))) {
            return null;
        }

        ValueWrapper cached = delegate.get(key);
        if (cached != null) {
            Object value = cached.get();
            if (value instanceof NullValue) {
                return null;
            }
            return (T) value;
        }

        String lockKey = "lock:cache:" + name + ":" + cacheKey;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(LOCK_WAIT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    ValueWrapper doubleCheck = delegate.get(key);
                    if (doubleCheck != null) {
                        Object value = doubleCheck.get();
                        if (value instanceof NullValue) {
                            return null;
                        }
                        return (T) value;
                    }

                    T value = valueLoader.call();
                    put(key, value);
                    return value;
                } finally {
                    lock.unlock();
                }
            }

            for (int i = 0; i < 4; i++) {
                TimeUnit.MILLISECONDS.sleep(25 * (i + 1));

                if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(nullKey(cacheKey)))) {
                    return null;
                }

                ValueWrapper fallbackCheck = delegate.get(key);
                if (fallbackCheck != null) {
                    Object v = fallbackCheck.get();
                    if (v instanceof NullValue) {
                        return null;
                    }
                    return (T) v;
                }
            }

            return valueLoader.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void put(Object key, Object value) {
        String cacheKey = toCacheKey(key);

        if (value == null) {
            stringRedisTemplate.opsForValue().set(nullKey(cacheKey), "NULL_MARKER", nullValueTtl);
            delegate.evict(key);
            return;
        }

        bloomFilterService.add(name, cacheKey);
        delegate.put(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        String cacheKey = toCacheKey(key);

        ValueWrapper existing = get(key);
        if (existing != null) {
            return existing;
        }

        put(key, value);
        return null;
    }

    @Override
    public void evict(Object key) {
        String cacheKey = toCacheKey(key);
        stringRedisTemplate.delete(nullKey(cacheKey));
        delegate.evict(key);
    }

    @Override
    public void clear() {
        log.warn("CachePenetrationProtectionCache.clear() called for [{}] - null markers may not be fully cleared", name);
        delegate.clear();
    }

    private String nullKey(String key) {
        return "null:" + name + ":" + key;
    }

    private String toCacheKey(Object key) {
        if (key == null) {
            return "null";
        }
        return key.toString();
    }
}

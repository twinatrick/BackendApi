package com.example.backendApi.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host:}")
    private String configuredHost;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${app.in-docker:false}")
    private boolean inDocker;

    @Value("${redis.cache.ttl-hours:1}")
    private long cacheTtlHours;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        String host = resolveRedisHost();
        
        LOGGER.info("初始化 Redis 連線 - Host: {}, Port: {}", host, port);
        
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 配置 ObjectMapper 支援多態類型
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        
        // 建立 JSON 序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = 
            new GenericJackson2JsonRedisSerializer(objectMapper);

        // 預設快取配置（從 application.yml 讀取，預設 1 小時）
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(cacheTtlHours))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()
                )
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
            );

        // 使用者快取配置（2 小時）
        RedisCacheConfiguration usersConfig = defaultConfig
            .entryTtl(Duration.ofHours(2));

        // AlertCheckLimit 快取配置（與預設相同，1 小時）
        RedisCacheConfiguration alertCheckLimitConfig = defaultConfig
            .entryTtl(Duration.ofHours(cacheTtlHours));

        // AquarkData 快取配置（與預設相同，1 小時）
        RedisCacheConfiguration aquarkDataConfig = defaultConfig
            .entryTtl(Duration.ofHours(cacheTtlHours));

        LOGGER.info("Redis 快取配置完成 - 預設 TTL: {} 小時, users: 2 小時, 序列化: JSON", cacheTtlHours);

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration("users", usersConfig)
            .withCacheConfiguration("alertCheckLimit", alertCheckLimitConfig)
            .withCacheConfiguration("aquarkData", aquarkDataConfig)
            .build();
    }

    /**
     * 智慧判斷 Redis 連線位址
     * 模仿 KafkaConfig 的 resolveBootstrapServers() 邏輯
     */
    private String resolveRedisHost() {
        if (configuredHost != null && !configuredHost.isBlank()) {
            LOGGER.info("Redis host 來自配置: {}", configuredHost);
            return configuredHost;
        }
        
        String resolved = inDocker ? "redis" : "localhost";
        LOGGER.info("Redis host 根據 APP_IN_DOCKER({}) 解析為: {}", inDocker, resolved);
        return resolved;
    }
}

package com.example.BackendApi.Config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

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

        // 參考資料 - 技能（24 小時）
        RedisCacheConfiguration skillsConfig = defaultConfig
            .entryTtl(Duration.ofHours(24));

        // 參考資料 - 技能等級（24 小時）
        RedisCacheConfiguration skillLevelsConfig = defaultConfig
            .entryTtl(Duration.ofHours(24));

        // 參考資料 - 角色（6 小時）
        RedisCacheConfiguration rolesConfig = defaultConfig
            .entryTtl(Duration.ofHours(6));

        // 參考資料 - 角色功能（6 小時）
        RedisCacheConfiguration roleFunctionsConfig = defaultConfig
            .entryTtl(Duration.ofHours(6));

        // 參考資料 - 功能（24 小時）
        RedisCacheConfiguration functionsConfig = defaultConfig
            .entryTtl(Duration.ofHours(24));

        // 業務資料 - 公司（6 小時）
        RedisCacheConfiguration companiesConfig = defaultConfig
            .entryTtl(Duration.ofHours(6));

        // 業務資料 - 職缺（1 小時）
        RedisCacheConfiguration jobPostingsConfig = defaultConfig
            .entryTtl(Duration.ofHours(1));

        // 業務資料 - 專案技能（30 分鐘）
        RedisCacheConfiguration projectSkillsConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(30));

        // 使用者資料 - 目前使用者專案（10 分鐘）
        RedisCacheConfiguration userProjectsConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(10));

        // 使用者資料 - 目前使用者技能（10 分鐘）
        RedisCacheConfiguration currentUserSkillsConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(10));

        // 使用者資料 - 使用者職缺連結（10 分鐘）
        RedisCacheConfiguration userJobLinksConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(10));

        // 使用者資料 - 使用者角色（10 分鐘）
        RedisCacheConfiguration userRolesConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(10));

        // 運算資料 - 平均數據（30 分鐘）
        RedisCacheConfiguration aquarkDataAvgConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(30));

        LOGGER.info("Redis 快取配置完成 - 預設 TTL: {} 小時, users: 2 小時, skills: 24 小時, roles: 6 小時, functions: 24 小時", cacheTtlHours);

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration("users", usersConfig)
            .withCacheConfiguration("alertCheckLimit", alertCheckLimitConfig)
            .withCacheConfiguration("aquarkData", aquarkDataConfig)
            .withCacheConfiguration("skills", skillsConfig)
            .withCacheConfiguration("skillLevels", skillLevelsConfig)
            .withCacheConfiguration("roles", rolesConfig)
            .withCacheConfiguration("roleFunctions", roleFunctionsConfig)
            .withCacheConfiguration("functions", functionsConfig)
            .withCacheConfiguration("companies", companiesConfig)
            .withCacheConfiguration("jobPostings", jobPostingsConfig)
            .withCacheConfiguration("projectSkills", projectSkillsConfig)
            .withCacheConfiguration("projectMemberSkills", projectSkillsConfig)
            .withCacheConfiguration("userProjects", userProjectsConfig)
            .withCacheConfiguration("currentUserSkills", currentUserSkillsConfig)
            .withCacheConfiguration("userJobLinks", userJobLinksConfig)
            .withCacheConfiguration("userRoles", userRolesConfig)
            .withCacheConfiguration("aquarkDataAvg", aquarkDataAvgConfig)
            .build();

        return cacheManager;
    }

    /**
     * 智慧判斷 Redis 連線位址
     * 模仿 KafkaConfig 的 resolveBootstrapServers() 邏輯
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                if (exception instanceof SerializationException se) {
                    LOGGER.warn("Redis 反序列化失敗，清除快取 [{}] key: {}", cache.getName(), key, se);
                    cache.evict(key);
                    return;
                }
                throw exception;
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                throw exception;
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                throw exception;
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                throw exception;
            }
        };
    }

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

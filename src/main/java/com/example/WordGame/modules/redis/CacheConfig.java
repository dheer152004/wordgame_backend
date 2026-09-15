package com.example.WordGame.modules.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(ObjectProvider<RedisConnectionFactory> connectionFactoryProvider) {
        RedisConnectionFactory connectionFactory = connectionFactoryProvider.getIfAvailable();
        if (connectionFactory == null) {
            log.warn("RedisConnectionFactory not available; using simple in-memory cache manager.");
            return simpleCacheManager();
        }

        if (!isRedisAvailable(connectionFactory)) {
            log.warn("Redis is unavailable; falling back to simple in-memory cache manager.");
            return simpleCacheManager();
        }

        // Default cache configuration - 10 minutes
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // Custom TTL for different caches
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Categories - 30 minutes (rarely changes)
        cacheConfigurations.put("categories",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));

        // Words feed - 5 minutes (frequent changes)
        cacheConfigurations.put("words",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(5))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));

        // Word details - 15 minutes
        cacheConfigurations.put("wordDetails",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(15))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));

        // ✅ ADDED: Random words cache - 10 minutes (balanced for randomness and performance)
        cacheConfigurations.put("randomWords",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));

        // Quiz questions - 1 day (changes daily)
        cacheConfigurations.put("quizQuestions",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofDays(1))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));

        // User stats - 1 minute (frequent updates)
        cacheConfigurations.put("userStats",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(1))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));

        // Quiz history - 5 minutes
        cacheConfigurations.put("quizHistory",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(5))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));

        cacheConfigurations.put("savedWordsCount", defaultConfig);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    private boolean isRedisAvailable(RedisConnectionFactory connectionFactory) {
        try (RedisConnection connection = connectionFactory.getConnection()) {
            return connection.ping() != null;
        } catch (RedisConnectionFailureException | RedisSystemException ex) {
            log.warn("Redis health check failed: {}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            log.warn("Unexpected error while checking Redis availability: {}", ex.getMessage());
            return false;
        }
    }

    private CacheManager simpleCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
                "categories",
                "words",
                "wordDetails",
                "randomWords",
                "quizQuestions",
                "userStats",
                "quizHistory",
                "savedWordsCount"
        );
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}

package com.enterprise.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for rate limiting and caching in API Gateway.
 */
@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {

        RedisSerializationContext<String, String> context = RedisSerializationContext
            .<String, String>newSerializationContext(new StringRedisSerializer())
            .key(new StringRedisSerializer())
            .value(new StringRedisSerializer())
            .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}

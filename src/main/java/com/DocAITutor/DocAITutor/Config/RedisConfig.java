package com.DocAITutor.DocAITutor.Config;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import io.lettuce.core.dynamic.annotation.Value;
import redis.clients.jedis.JedisPooled;

//@Configuration
//public class RedisConfig {
//    
//    @Value("${redis.host}")
//    private String redisHost;
//    
//    @Value("${redis.port}")
//    private int redisPort;
//    
//    @Value("${redis.username}")
//    private String redisUsername;
//    
//    @Value("${redis.password}")
//    private String redisPassword;
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
//        return template;
//    }
//    @Bean
//    public JedisPooled jedisPooled() {
//        // Configure connection pooling
//        redis.clients.jedis.ConnectionPoolConfig poolConfig = new redis.clients.jedis.ConnectionPoolConfig();
//        poolConfig.setMaxTotal(8);    // Maximum number of connections
//        poolConfig.setMaxIdle(8);     // Maximum number of idle connections
//        poolConfig.setMinIdle(0);     // Minimum number of idle connections
//        // Create JedisPooled with authentication
//        return new JedisPooled(poolConfig,
//                redisHost,
//                redisPort,
//                redisUsername,
//                redisPassword);
//    }
//}
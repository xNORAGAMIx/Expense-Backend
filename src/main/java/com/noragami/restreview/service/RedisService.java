package com.noragami.restreview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    // Limit to 1 request per 60 seconds
    private static final long LIMIT_WINDOW_SECONDS = 60;

    public boolean isBlocked(String key) {
        String redisKey = "ratelimit:" + key;
        Long count = redisTemplate.opsForValue().increment(redisKey);

        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(LIMIT_WINDOW_SECONDS));
        }

        return count != null && count > 1;
    }
}

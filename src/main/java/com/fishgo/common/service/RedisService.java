package com.fishgo.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 해당 키가 이미 존재하는지 확인합니다.
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 특정 키에 대한 값을 저장하고, 지정된 시간 이후 만료되도록 설정합니다.
     */
    public void setWithExpire(String key, Object value, long timeout, TimeUnit unit) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value, timeout, unit);
    }

    /**
     * 필요한 다른 Redis 작업도 이곳에서 메서드로 정의할 수 있습니다.
     */
    // 예: delete, increment, 등등
}

package justtodobe.service.impl;

import jakarta.annotation.Resource;
import justtodobe.config.JwtProperties;
import justtodobe.service.RedisAuthService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis 登录态存储实现。
 * Key: auth:token:{token}, Value: userId, TTL 与 JWT 过期保持一致。
 */
@Service("redisAuthService")
public class RedisAuthServiceImpl implements RedisAuthService {

    private static final String KEY_PREFIX = "auth:token:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private JwtProperties jwtProperties;

    @Override
    public void saveToken(String token, Long userId, long ttlSeconds) {
        String key = KEY_PREFIX + token;
        stringRedisTemplate
                .opsForValue()
                .set(key, String.valueOf(userId), ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean existsToken(String token) {
        if (token == null || token.isBlank()) return false;
        Boolean has = stringRedisTemplate.hasKey(KEY_PREFIX + token);
        return Boolean.TRUE.equals(has);
    }

    @Override
    public void removeToken(String token) {
        if (token == null || token.isBlank()) return;
        stringRedisTemplate.delete(KEY_PREFIX + token);
    }

    @Override
    public long getDefaultTtlSeconds() {
        return jwtProperties.getExpireSeconds();
    }
}


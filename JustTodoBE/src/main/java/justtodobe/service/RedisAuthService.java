package justtodobe.service;

import jakarta.annotation.Resource;
import justtodobe.config.JwtProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录态 Redis 存储：与 JWT 双重校验，登出可立即失效。
 * Key: auth:token:{token}, Value: userId, TTL 与 JWT 过期一致。
 */
@Service
public class RedisAuthService {

    private static final String KEY_PREFIX = "auth:token:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private JwtProperties jwtProperties;

    public void saveToken(String token, Long userId, long ttlSeconds) {
        String key = KEY_PREFIX + token;
        stringRedisTemplate.opsForValue().set(key, String.valueOf(userId), ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean existsToken(String token) {
        if (token == null || token.isBlank()) return false;
        Boolean has = stringRedisTemplate.hasKey(KEY_PREFIX + token);
        return Boolean.TRUE.equals(has);
    }

    public void removeToken(String token) {
        if (token == null || token.isBlank()) return;
        stringRedisTemplate.delete(KEY_PREFIX + token);
    }

    /** 使用 JWT 配置的过期时间（秒） */
    public long getDefaultTtlSeconds() {
        return jwtProperties.getExpireSeconds();
    }
}

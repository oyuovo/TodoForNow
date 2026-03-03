package justtodobe.service;

/**
 * 登录态在 Redis 中的存储服务。
 */
public interface RedisAuthService {

    void saveToken(String token, Long userId, long ttlSeconds);

    boolean existsToken(String token);

    void removeToken(String token);

    /**
     * 默认的过期时间（秒），通常与 JWT 配置保持一致。
     */
    long getDefaultTtlSeconds();
}

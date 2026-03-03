package justtodobe.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "todo-cache")
public class TodoCacheProperties {
    /** 缓存 TTL 基准秒数 */
    private int ttlBaseSeconds = 300;
    /** 缓存 TTL 随机范围秒数，实际 TTL = ttlBaseSeconds + random(0, ttlRangeSeconds) */
    private int ttlRangeSeconds = 600;

    public int getTtlBaseSeconds() {
        return ttlBaseSeconds;
    }

    public void setTtlBaseSeconds(int ttlBaseSeconds) {
        this.ttlBaseSeconds = ttlBaseSeconds;
    }

    public int getTtlRangeSeconds() {
        return ttlRangeSeconds;
    }

    public void setTtlRangeSeconds(int ttlRangeSeconds) {
        this.ttlRangeSeconds = ttlRangeSeconds;
    }
}

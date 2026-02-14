package justtodobe.utils;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import justtodobe.config.JwtProperties;
import justtodobe.entity.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.currentTimeMillis;

@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("expire_time", currentTimeMillis() + jwtProperties.getExpireSeconds() * 1000);
        return JWTUtil.createToken(map, jwtProperties.getSecret().getBytes());
    }

    public boolean verify(String token) {
        if (token == null) return false;
        try {
            JWT jwt = JWTUtil.parseToken(token);
            Long expireTime = cn.hutool.core.convert.Convert.toLong(jwt.getPayload().getClaim("expire_time"));
            if (expireTime == null || expireTime <= currentTimeMillis()) {
                return false;
            }
            return JWTUtil.verify(token, jwtProperties.getSecret().getBytes());
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        if (token == null) return null;
        try {
            JWT jwt = JWTUtil.parseToken(token);
            return cn.hutool.core.convert.Convert.toLong(jwt.getPayload().getClaim("id"));
        } catch (Exception e) {
            return null;
        }
    }
}

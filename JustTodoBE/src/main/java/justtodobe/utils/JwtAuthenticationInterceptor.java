package justtodobe.utils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import justtodobe.DTO.ResultDTO;
import justtodobe.service.RedisAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private RedisAuthService redisAuthService;

    private void writeUnauthorized(HttpServletResponse response, String message) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        try {
            // 与前端 httpRequest/httpRequestUnwrap 期望的 ResultDTO 结构对齐
            String json = String.format("{\"success\":false,\"errorMsg\":\"%s\",\"data\":null}", message);
            response.getWriter().write(json);
        } catch (IOException e) {
            log.warn("写入未授权响应失败: {}", e.getMessage());
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.verify(token)) {
            writeUnauthorized(response, "登录状态已失效，请重新登录");
            return false;
        }
        if (!redisAuthService.existsToken(token)) {
            writeUnauthorized(response, "登录状态已失效，请重新登录");
            return false;
        }
        Long userId = jwtUtil.getUserId(token);
        if (userId != null) {
            UserContext.setUserId(userId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}

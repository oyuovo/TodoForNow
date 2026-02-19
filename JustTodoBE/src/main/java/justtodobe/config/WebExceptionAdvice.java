package justtodobe.config;

import justtodobe.DTO.ResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultDTO handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("请求参数校验失败");
        log.warn("参数校验失败: {}", message);
        return ResultDTO.fail(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResultDTO handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return ResultDTO.fail("请求参数格式错误，请检查 JSON 格式及字段类型");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResultDTO handleRuntimeException(RuntimeException e) {
        log.error(e.toString(), e);
        return ResultDTO.fail("服务器内部错误，请稍后重试");
    }
}

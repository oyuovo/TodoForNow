package justtodobe.config;

import justtodobe.DTO.ResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler(RuntimeException.class)
    public ResultDTO handleRuntimeException(RuntimeException e) {
        log.error(e.toString(), e);
        return ResultDTO.fail("服务器内部错误，请稍后重试");
    }
}

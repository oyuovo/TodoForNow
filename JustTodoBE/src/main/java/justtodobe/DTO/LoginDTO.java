package justtodobe.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginDTO {
    @NotNull(message = "用户ID不能为空")
    private Long id;

    @NotBlank(message = "密码不能为空")
    private String password;
}

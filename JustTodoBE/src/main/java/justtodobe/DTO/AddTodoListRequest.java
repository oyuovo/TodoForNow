package justtodobe.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddTodoListRequest {
    @NotBlank(message = "清单名称不能为空")
    @Size(max = 20, message = "清单名称不超过20个字符")
    private String name;
}

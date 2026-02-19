package justtodobe.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddTodoRequest {
    @NotBlank(message = "待办内容不能为空")
    @Size(max = 100, message = "待办内容不超过100个字符")
    private String context;

    @NotBlank(message = "待办ID不能为空")
    private String id;

    /** 0=非定时 1=定时，默认0 */
    @Min(value = 0, message = "timeset 需为 0 或 1")
    @Max(value = 1, message = "timeset 需为 0 或 1")
    private Integer timeset;
}

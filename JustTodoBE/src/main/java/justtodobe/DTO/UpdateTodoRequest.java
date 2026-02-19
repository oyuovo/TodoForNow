package justtodobe.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTodoRequest {
    @Size(max = 100, message = "待办内容不超过100个字符")
    private String context;

    /** 3=标记定时任务已完成 */
    @Min(value = 0, message = "timeset 需为 0、1 或 3")
    @Max(value = 3, message = "timeset 需为 0、1 或 3")
    private Integer timeset;
}

package justtodobe.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 清除已完成待办的请求体，传入待删除的 itemId 列表 */
@Data
public class ClearCompletedRequest {
    @NotNull(message = "itemIds 不能为空")
    private List<String> itemIds;
}

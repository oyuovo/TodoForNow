package justtodobe.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SetMemoPathRequest {
    @NotBlank(message = "备忘录路径不能为空")
    @Size(max = 512, message = "路径不超过512个字符")
    private String memopath;
}

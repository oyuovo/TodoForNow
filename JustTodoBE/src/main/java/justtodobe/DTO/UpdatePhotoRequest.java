package justtodobe.DTO;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePhotoRequest {
    @Size(max = 500, message = "头像地址不超过500个字符")
    private String photo;
}

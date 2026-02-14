package justtodobe.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_user")
public class User {
    private String username;
    private String password;
    private String phone;
    private String photo;
    @TableId("id")
    private Long id;
    private String memopath;
}

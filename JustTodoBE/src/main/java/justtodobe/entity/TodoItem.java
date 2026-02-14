package justtodobe.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_todoitem")
public class TodoItem {
    @TableId("itemid")
    String itemid;
    String context;
    String listid;
    LocalDateTime createtime;
    LocalDateTime updatetime;
}

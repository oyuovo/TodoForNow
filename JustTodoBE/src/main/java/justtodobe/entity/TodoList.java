package justtodobe.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_todolist")
public class TodoList {
    @TableId("listid")
    String listid;
    String name;
}

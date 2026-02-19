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
    /** 0=非定时 1=定时未完成 3=定时已完成(前端不显示,定时恢复为1) */
    Integer timeset;
    LocalDateTime createtime;
    LocalDateTime updatetime;
}

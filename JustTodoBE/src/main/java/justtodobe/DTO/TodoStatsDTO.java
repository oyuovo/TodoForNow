package justtodobe.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoStatsDTO {
    /** 当前用户下的清单总数 */
    private long totalLists;
    /** 所有清单下的待办总数（含普通与定时） */
    private long totalTodos;
    /** 定时任务总数（timeset=1 或 3） */
    private long scheduledTotal;
    /** 已完成的定时任务数量（timeset=3） */
    private long scheduledCompleted;
    /** 普通待办总数（timeset=0） */
    private long normalTotal;
}


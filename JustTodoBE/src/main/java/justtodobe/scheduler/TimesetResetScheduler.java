package justtodobe.scheduler;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import justtodobe.entity.TodoItem;
import justtodobe.mapper.TodoItemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务：将 timeset=3（定时任务已完成）恢复为 1（定时任务未完成），次日重新显示。
 * 默认每天 0 点执行。
 */
@Component
public class TimesetResetScheduler {

    private static final Logger log = LoggerFactory.getLogger(TimesetResetScheduler.class);

    @Resource
    private TodoItemMapper todoItemMapper;

    @Scheduled(cron = "0 0 0 * * ?")
    public void resetTimesetCompletedToPending() {
        UpdateWrapper<TodoItem> wrapper = new UpdateWrapper<>();
        wrapper.eq("timeset", 3).set("timeset", 1);
        int updated = todoItemMapper.update(null, wrapper);
        if (updated > 0) {
            log.info("定时恢复：已将 {} 条 timeset=3 的待办恢复为未完成", updated);
        }
    }
}

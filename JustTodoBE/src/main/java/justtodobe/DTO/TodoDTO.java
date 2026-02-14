package justtodobe.DTO;

import justtodobe.entity.TodoItem;
import lombok.Data;

import java.util.List;

@Data
public class TodoDTO {
    String listid;
    String name;
    List<TodoItem> todoItem;
}

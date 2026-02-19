package justtodobe.service;

import justtodobe.DTO.ResultDTO;

import java.util.List;

public interface TodoListService {
    ResultDTO getTodoList();

    ResultDTO getTodoListWithoutTodos();

    ResultDTO addTodoList(String name);

    ResultDTO updateTodoList(String listId, String name);

    ResultDTO deleteTodoList(String listId);

    ResultDTO addTodo(String listId, String context, String todoId, Integer timeset);

    ResultDTO updateTodo(String listId, String todoId, String context, Integer timeset);

    ResultDTO deleteTodo(String listId, String todoId);

    /** 批量删除指定清单下的多个待办（用于「清除已完成」） */
    ResultDTO clearCompleted(String listId, List<String> itemIds);
}

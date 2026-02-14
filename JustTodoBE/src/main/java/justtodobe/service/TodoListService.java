package justtodobe.service;

import justtodobe.DTO.ResultDTO;

public interface TodoListService {
    ResultDTO getTodoList();

    ResultDTO getTodoListWithoutTodos();

    ResultDTO addTodoList(String name);

    ResultDTO updateTodoList(String listId, String name);

    ResultDTO deleteTodoList(String listId);

    ResultDTO addTodo(String listId, String context, String todoId);

    ResultDTO updateTodo(String listId, String todoId, String context);

    ResultDTO deleteTodo(String listId, String todoId);
}

package justtodobe.controller;

import jakarta.annotation.Resource;
import justtodobe.DTO.AddTodoListRequest;
import justtodobe.DTO.AddTodoRequest;
import justtodobe.DTO.ResultDTO;
import justtodobe.DTO.UpdateTodoListRequest;
import justtodobe.DTO.UpdateTodoRequest;
import justtodobe.service.TodoListService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/todo-lists")
public class TodoListController {
    @Resource
    private TodoListService todoListService;

    @GetMapping()
    public ResultDTO getTodoList(@RequestParam("includeTodos") Boolean includeTodos) {
        return Boolean.TRUE.equals(includeTodos) ? todoListService.getTodoList() : todoListService.getTodoListWithoutTodos();
    }

    @PostMapping()
    public ResultDTO addTodoList(@RequestBody AddTodoListRequest request) {
        String name = request != null && request.getName() != null ? request.getName() : "";
        return todoListService.addTodoList(name);
    }

    @PatchMapping("/{listId}")
    public ResultDTO updateTodoList(@RequestBody UpdateTodoListRequest request, @PathVariable("listId") String listId) {
        String name = request != null && request.getName() != null ? request.getName() : "";
        return todoListService.updateTodoList(listId, name);
    }

    @DeleteMapping("/{listId}")
    public ResultDTO deleteTodoList(@PathVariable("listId") String listId) {
        return todoListService.deleteTodoList(listId);
    }

    @PostMapping("/{listId}/todos")
    public ResultDTO addTodo(@RequestBody AddTodoRequest request, @PathVariable("listId") String listId) {
        String context = request != null && request.getContext() != null ? request.getContext() : "";
        String todoId = request != null && request.getId() != null ? request.getId() : "";
        return todoListService.addTodo(listId, context, todoId);
    }

    @PatchMapping("/{listId}/todos/{todoId}")
    public ResultDTO updateTodo(@RequestBody UpdateTodoRequest request,
                                @PathVariable("listId") String listId,
                                @PathVariable("todoId") String todoId) {
        String context = request != null && request.getContext() != null ? request.getContext() : "";
        return todoListService.updateTodo(listId, todoId, context);
    }

    @DeleteMapping("/{listId}/todos/{todoId}")
    public ResultDTO deleteTodo(@PathVariable("listId") String listId,
                                @PathVariable("todoId") String todoId) {
        return todoListService.deleteTodo(listId, todoId);
    }
}

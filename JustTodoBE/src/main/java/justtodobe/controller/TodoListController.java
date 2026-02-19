package justtodobe.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import java.util.List;
import justtodobe.DTO.AddTodoListRequest;
import justtodobe.DTO.AddTodoRequest;
import justtodobe.DTO.ClearCompletedRequest;
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
    public ResultDTO addTodoList(@Valid @RequestBody AddTodoListRequest request) {
        return todoListService.addTodoList(request.getName());
    }

    @PatchMapping("/{listId}")
    public ResultDTO updateTodoList(@Valid @RequestBody UpdateTodoListRequest request, @PathVariable("listId") String listId) {
        return todoListService.updateTodoList(listId, request.getName());
    }

    @DeleteMapping("/{listId}")
    public ResultDTO deleteTodoList(@PathVariable("listId") String listId) {
        return todoListService.deleteTodoList(listId);
    }

    @PostMapping("/{listId}/todos")
    public ResultDTO addTodo(@Valid @RequestBody AddTodoRequest request, @PathVariable("listId") String listId) {
        Integer timeset = request.getTimeset() != null ? request.getTimeset() : 0;
        return todoListService.addTodo(listId, request.getContext(), request.getId(), timeset);
    }

    @PatchMapping("/{listId}/todos/{todoId}")
    public ResultDTO updateTodo(@Valid @RequestBody UpdateTodoRequest request,
                                @PathVariable("listId") String listId,
                                @PathVariable("todoId") String todoId) {
        return todoListService.updateTodo(listId, todoId, request.getContext(), request.getTimeset());
    }

    @DeleteMapping("/{listId}/todos/{todoId}")
    public ResultDTO deleteTodo(@PathVariable("listId") String listId,
                                @PathVariable("todoId") String todoId) {
        return todoListService.deleteTodo(listId, todoId);
    }

    @DeleteMapping("/{listId}/todos/completed")
    public ResultDTO clearCompleted(@Valid @RequestBody ClearCompletedRequest request,
                                    @PathVariable("listId") String listId) {
        return todoListService.clearCompleted(listId, request.getItemIds());
    }
}

package justtodobe.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import justtodobe.DTO.ResultDTO;
import justtodobe.DTO.TodoDTO;
import justtodobe.entity.TodoItem;
import justtodobe.entity.TodoList;
import justtodobe.mapper.TodoItemMapper;
import justtodobe.mapper.TodoListMapper;
import justtodobe.service.TodoListService;
import justtodobe.utils.UserContext;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TodoListServiceImpl implements TodoListService {
    @Resource
    private TodoListMapper todoListMapper;
    @Resource
    private TodoItemMapper todoItemMapper;

    /** listid 格式为 {用户id}T{清单序号}，如 1T1。前缀用于按当前用户筛选清单。 */
    private static String listIdPrefixForCurrentUser() {
        Long userId = UserContext.getUserId();
        return userId != null ? userId + "T" : null;
    }

    /** 校验 listId 是否属于当前登录用户 */
    private boolean isListOwnedByCurrentUser(String listId) {
        String prefix = listIdPrefixForCurrentUser();
        if (prefix == null) return false;
        return listId != null && listId.startsWith(prefix);
    }

    @Override
    public ResultDTO getTodoListWithoutTodos() {
        String prefix = listIdPrefixForCurrentUser();
        if (prefix == null) return ResultDTO.fail("用户未登录");
        QueryWrapper<TodoList> wrapper = new QueryWrapper<>();
        wrapper.likeRight("listid", prefix);
        List<TodoList> todoList = todoListMapper.selectList(wrapper);
        return ResultDTO.ok(todoList);
    }

    @Override
    @Transactional
    public ResultDTO addTodoList(String name) {
        String prefix = listIdPrefixForCurrentUser();
        if (prefix == null) return ResultDTO.fail("用户未登录");
        long count = todoListMapper.selectCount(new QueryWrapper<TodoList>().likeRight("listid", prefix));
        String listId = prefix + (count + 1);
        TodoList todoList = new TodoList();
        todoList.setListid(listId);
        todoList.setName(name);
        int insert = todoListMapper.insert(todoList);
        return insert > 0 ? ResultDTO.ok(todoList) : ResultDTO.fail("添加清单失败");
    }

    @Override
    @Transactional
    public ResultDTO updateTodoList(String listId, String name) {
        if (!isListOwnedByCurrentUser(listId)) {
            return ResultDTO.fail("无权操作该清单");
        }
        TodoList todoList = new TodoList();
        todoList.setListid(listId);
        todoList.setName(name);
        int update = todoListMapper.update(todoList, new QueryWrapper<TodoList>().eq("listid", listId));
        return update > 0 ? ResultDTO.ok() : ResultDTO.fail("更新清单失败");
    }

    @Override
    @Transactional
    public ResultDTO deleteTodoList(String listId) {
        if (!isListOwnedByCurrentUser(listId)) {
            return ResultDTO.fail("无权操作该清单");
        }
        todoItemMapper.delete(new QueryWrapper<TodoItem>().eq("listid", listId));
        int delete = todoListMapper.delete(new QueryWrapper<TodoList>().eq("listid", listId));
        return delete > 0 ? ResultDTO.ok() : ResultDTO.fail("删除清单失败");
    }

    @Override
    @Transactional
    public ResultDTO addTodo(String listId, String context, String todoId, Integer timeset) {
        if (!isListOwnedByCurrentUser(listId)) {
            return ResultDTO.fail("无权操作该清单");
        }
        TodoItem todoItem = new TodoItem();
        todoItem.setItemid(todoId);
        todoItem.setListid(listId);
        todoItem.setContext(context);
        todoItem.setTimeset(timeset != null ? timeset : 0);
        todoItem.setCreatetime(LocalDateTime.now());
        todoItem.setUpdatetime(LocalDateTime.now());
        int insert = todoItemMapper.insert(todoItem);
        return insert > 0 ? ResultDTO.ok() : ResultDTO.fail("添加待办失败");
    }

    @Override
    @Transactional
    public ResultDTO updateTodo(String listId, String todoId, String context, Integer timeset) {
        if (!isListOwnedByCurrentUser(listId)) {
            return ResultDTO.fail("无权操作该清单");
        }
        UpdateWrapper<TodoItem> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("listid", listId)
                .eq("itemid", todoId)
                .set("updatetime", LocalDateTime.now());
        if (context != null) {
            updateWrapper.set("context", context);
        }
        if (timeset != null) {
            updateWrapper.set("timeset", timeset);
        }
        int update = todoItemMapper.update(null, updateWrapper);
        return update > 0 ? ResultDTO.ok() : ResultDTO.fail("更新待办失败，listId 或 itemId 不匹配");
    }

    @Override
    public ResultDTO deleteTodo(String listId, String todoId) {
        if (!isListOwnedByCurrentUser(listId)) {
            return ResultDTO.fail("无权操作该清单");
        }
        int delete = todoItemMapper.delete(new QueryWrapper<TodoItem>().eq("listid", listId).eq("itemid", todoId));
        return delete > 0 ? ResultDTO.ok() : ResultDTO.fail("删除待办失败，listId 或 itemId 不匹配");
    }

    @Override
    @Transactional
    public ResultDTO clearCompleted(String listId, List<String> itemIds) {
        if (!isListOwnedByCurrentUser(listId)) {
            return ResultDTO.fail("无权操作该清单");
        }
        if (itemIds == null || itemIds.isEmpty()) {
            return ResultDTO.ok();
        }
        int deleted = todoItemMapper.delete(
                new QueryWrapper<TodoItem>().eq("listid", listId).in("itemid", itemIds));
        return ResultDTO.ok(Collections.singletonMap("deleted", deleted));
    }

    @Override
    public ResultDTO getTodoList() {
        String prefix = listIdPrefixForCurrentUser();
        if (prefix == null) return ResultDTO.fail("用户未登录");
        QueryWrapper<TodoList> wrapper = new QueryWrapper<>();
        wrapper.likeRight("listid", prefix);
        List<TodoList> todoList = todoListMapper.selectList(wrapper);
        if (todoList.isEmpty()) return ResultDTO.ok(List.of());

        List<String> listIds = todoList.stream().map(TodoList::getListid).toList();
        List<TodoItem> allItems = todoItemMapper.selectList(
                new QueryWrapper<TodoItem>().in("listid", listIds));
        Map<String, List<TodoItem>> itemsByListId = allItems.stream()
                .collect(Collectors.groupingBy(TodoItem::getListid));

        List<TodoDTO> todoListDTO = todoList.stream().map(tl -> {
            TodoDTO dto = new TodoDTO();
            dto.setListid(tl.getListid());
            dto.setName(tl.getName());
            dto.setTodoItem(itemsByListId.getOrDefault(tl.getListid(), List.of()));
            return dto;
        }).toList();
        return ResultDTO.ok(todoListDTO);
    }
}

package justtodobe.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import justtodobe.DTO.ResultDTO;
import justtodobe.DTO.TodoDTO;
import justtodobe.config.TodoCacheProperties;
import justtodobe.entity.TodoItem;
import justtodobe.entity.TodoList;
import justtodobe.mapper.TodoItemMapper;
import justtodobe.mapper.TodoListMapper;
import justtodobe.service.TodoListService;
import justtodobe.utils.UserContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TodoListServiceImpl implements TodoListService {

    private static final String CACHE_KEY_PREFIX = "todo:list:";
    private static final TypeReference<List<TodoDTO>> TODO_LIST_TYPE = new TypeReference<>() {};

    @Resource
    private TodoListMapper todoListMapper;
    @Resource
    private TodoItemMapper todoItemMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private TodoCacheProperties todoCacheProperties;

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

    private String todoCacheKey() {
        Long userId = UserContext.getUserId();
        return userId != null ? CACHE_KEY_PREFIX + userId : null;
    }

    /** 使当前用户的待办列表缓存失效 */
    private void evictTodoListCache() {
        String key = todoCacheKey();
        if (key != null) {
            stringRedisTemplate.delete(key);
        }
    }

    /** 范围内随机 TTL（秒）：base + random(0, range) */
    private long randomTtlSeconds() {
        int base = todoCacheProperties.getTtlBaseSeconds();
        int range = Math.max(0, todoCacheProperties.getTtlRangeSeconds());
        return base + (range > 0 ? ThreadLocalRandom.current().nextInt(range + 1) : 0);
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
        if (insert > 0) {
            evictTodoListCache();
            return ResultDTO.ok(todoList);
        }
        return ResultDTO.fail("添加清单失败");
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
        if (update > 0) {
            evictTodoListCache();
            return ResultDTO.ok();
        }
        return ResultDTO.fail("更新清单失败");
    }

    @Override
    @Transactional
    public ResultDTO deleteTodoList(String listId) {
        if (!isListOwnedByCurrentUser(listId)) {
            return ResultDTO.fail("无权操作该清单");
        }
        todoItemMapper.delete(new QueryWrapper<TodoItem>().eq("listid", listId));
        int delete = todoListMapper.delete(new QueryWrapper<TodoList>().eq("listid", listId));
        if (delete > 0) {
            evictTodoListCache();
            return ResultDTO.ok();
        }
        return ResultDTO.fail("删除清单失败");
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
        if (insert > 0) {
            evictTodoListCache();
            return ResultDTO.ok();
        }
        return ResultDTO.fail("添加待办失败");
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
        if (update > 0) {
            evictTodoListCache();
            return ResultDTO.ok();
        }
        return ResultDTO.fail("更新待办失败，listId 或 itemId 不匹配");
    }

    @Override
    public ResultDTO deleteTodo(String listId, String todoId) {
        if (!isListOwnedByCurrentUser(listId)) {
            return ResultDTO.fail("无权操作该清单");
        }
        int delete = todoItemMapper.delete(new QueryWrapper<TodoItem>().eq("listid", listId).eq("itemid", todoId));
        if (delete > 0) {
            evictTodoListCache();
            return ResultDTO.ok();
        }
        return ResultDTO.fail("删除待办失败，listId 或 itemId 不匹配");
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
        if (deleted > 0) {
            evictTodoListCache();
        }
        return ResultDTO.ok(Collections.singletonMap("deleted", deleted));
    }

    @Override
    public ResultDTO getTodoList() {
        Long userId = UserContext.getUserId();
        if (userId == null) return ResultDTO.fail("用户未登录");

        String cacheKey = CACHE_KEY_PREFIX + userId;
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isBlank()) {
                List<TodoDTO> list = objectMapper.readValue(cached, TODO_LIST_TYPE);
                return ResultDTO.ok(list);
            }
        } catch (Exception ignored) {
            // 反序列化失败则回源查库
        }

        String prefix = listIdPrefixForCurrentUser();
        QueryWrapper<TodoList> wrapper = new QueryWrapper<>();
        wrapper.likeRight("listid", prefix);
        List<TodoList> todoList = todoListMapper.selectList(wrapper);
        if (todoList.isEmpty()) {
            return ResultDTO.ok(List.of());
        }

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

        try {
            String json = objectMapper.writeValueAsString(todoListDTO);
            long ttl = randomTtlSeconds();
            stringRedisTemplate.opsForValue().set(cacheKey, json, ttl, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // 序列化失败仅影响缓存，不影响返回
        }
        return ResultDTO.ok(todoListDTO);
    }
}

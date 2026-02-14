import type { TodoItem, TodoList } from '../types/todo';
import { httpRequestUnwrap } from './http';

interface BackendTodoItem {
  item_id?: string | null;
  context?: string;
  list_id?: string | null;
  create_time?: string | null;
  update_time?: string | null;
}

interface BackendTodoList {
  listid?: string | null;
  listId?: string | null;
  name?: string;
  todoItem?: BackendTodoItem[] | null;
}

function mapBackendListToFrontend(b: BackendTodoList): TodoList {
  const id = (b.listid ?? b.listId) != null ? String(b.listid ?? b.listId) : '';
  const todos: TodoItem[] = (b.todoItem ?? []).map((t) => ({
    id: t.item_id != null ? String(t.item_id) : '',
    title: t.context ?? '',
    completed: false,
    createdAt: t.create_time ?? new Date().toISOString(),
    updatedAt: t.update_time ?? new Date().toISOString(),
  }));
  return { id, name: b.name ?? '', todos };
}

/** 待办清单 & 待办项 API，统一走 { success, errorCode, data } 格式 */
export const todoApi = {
  async fetchLists(includeTodos = true): Promise<TodoList[]> {
    const query = includeTodos ? '?includeTodos=true' : '?includeTodos=false';
    const raw = await httpRequestUnwrap<BackendTodoList[] | null>(`/todo-lists${query}`, {
      method: 'GET',
    });
    const list = Array.isArray(raw) ? raw.filter(Boolean) : [];
    return list.map(mapBackendListToFrontend);
  },

  async createList(name: string): Promise<TodoList> {
    const raw = await httpRequestUnwrap<BackendTodoList | TodoList | null>('/todo-lists', {
      method: 'POST',
      body: JSON.stringify({ name }),
    });
    if (raw != null && typeof raw === 'object' && ('listid' in raw || 'listId' in raw)) {
      return mapBackendListToFrontend(raw as BackendTodoList);
    }
    // 业务成功但 data 为 null 或格式不符：用用户输入的 name 构造本地展示，刷新后会从数据库拉取
    const id =
      raw != null && typeof raw === 'object' && 'id' in raw && raw.id != null && raw.id !== ''
        ? String(raw.id)
        : crypto.randomUUID();
    const listName =
      raw != null && typeof raw === 'object' && 'name' in raw && raw.name != null
        ? String(raw.name)
        : name;
    return { id, name: listName, todos: Array.isArray((raw as { todos?: TodoItem[] })?.todos) ? (raw as { todos: TodoItem[] }).todos : [] };
  },

  async updateList(
    listId: string,
    payload: { name?: string },
  ): Promise<TodoList> {
    return httpRequestUnwrap<TodoList>(`/todo-lists/${encodeURIComponent(listId)}`, {
      method: 'PATCH',
      body: JSON.stringify(payload),
    });
  },

  async deleteList(listId: string): Promise<void> {
    await httpRequestUnwrap<void>(`/todo-lists/${encodeURIComponent(listId)}`, {
      method: 'DELETE',
    });
  },

  async fetchTodos(listId: string): Promise<TodoItem[]> {
    return httpRequestUnwrap<TodoItem[]>(
      `/todo-lists/${encodeURIComponent(listId)}/todos`,
      { method: 'GET' },
    );
  },

  /** 创建待办：仅需 id 和 context，格式如 { id: "T1", context: "待办内容" } */
  async createTodo(listId: string, payload: { id: string; context: string }): Promise<void> {
    if (!listId || listId.trim() === '') {
      throw new Error('清单 ID 不能为空，请先选择或创建清单');
    }
    await httpRequestUnwrap<TodoItem>(
      `/todo-lists/${encodeURIComponent(listId)}/todos`,
      {
        method: 'POST',
        body: JSON.stringify(payload),
      },
    );
  },

  /** 更新待办：仅需 context，格式如 { context: "新的待办内容" } */
  async updateTodo(
    listId: string,
    todoId: string,
    payload: { context: string },
  ): Promise<void> {
    await httpRequestUnwrap<TodoItem>(
      `/todo-lists/${encodeURIComponent(listId)}/todos/${encodeURIComponent(todoId)}`,
      {
        method: 'PATCH',
        body: JSON.stringify(payload),
      },
    );
  },

  async deleteTodo(listId: string, todoId: string): Promise<void> {
    await httpRequestUnwrap<void>(
      `/todo-lists/${encodeURIComponent(listId)}/todos/${encodeURIComponent(todoId)}`,
      {
        method: 'DELETE',
      },
    );
  },

  async clearCompleted(listId: string): Promise<void> {
    await httpRequestUnwrap<void>(
      `/todo-lists/${encodeURIComponent(listId)}/todos/completed`,
      {
        method: 'DELETE',
      },
    );
  },
};


import type { TodoItem, TodoList } from '../types/todo';
import { httpRequestUnwrap } from './http';

interface BackendTodoItem {
  item_id?: string | null;
  itemid?: string | null;
  context?: string;
  list_id?: string | null;
  listid?: string | null;
  timeset?: number | null;
  create_time?: string | null;
  createtime?: string | null;
  update_time?: string | null;
  updatetime?: string | null;
}

interface BackendTodoList {
  listid?: string | null;
  listId?: string | null;
  name?: string;
  todoItem?: BackendTodoItem[] | null;
}

function mapBackendListToFrontend(b: BackendTodoList): TodoList {
  const id = (b.listid ?? b.listId) != null ? String(b.listid ?? b.listId) : '';
  const rawItems = b.todoItem ?? [];
  const todos: TodoItem[] = rawItems
    .filter((t) => {
      const ts = t.timeset ?? 0;
      return ts !== 3;
    })
    .map((t) => ({
      id: (t.item_id ?? t.itemid) != null ? String(t.item_id ?? t.itemid) : '',
      title: t.context ?? '',
      completed: false,
      timeset: (t.timeset ?? 0) as 0 | 1 | 3,
      createdAt: (t.create_time ?? t.createtime) ?? new Date().toISOString(),
      updatedAt: (t.update_time ?? t.updatetime) ?? new Date().toISOString(),
    }));
  return { id, name: b.name ?? '', todos };
}

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
    // data 为空时用 name 构造本地展示
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

  async createTodo(
    listId: string,
    payload: { id: string; context: string; timeset?: number },
  ): Promise<void> {
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

  async updateTodo(
    listId: string,
    todoId: string,
    payload: { context?: string; timeset?: number },
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

  /** 批量删除处于「已完成」状态的待办（传入待删除的 itemId 列表） */
  async clearCompleted(listId: string, itemIds: string[]): Promise<void> {
    await httpRequestUnwrap<void>(
      `/todo-lists/${encodeURIComponent(listId)}/todos/completed`,
      {
        method: 'DELETE',
        body: JSON.stringify({ itemIds: itemIds ?? [] }),
      },
    );
  },
};


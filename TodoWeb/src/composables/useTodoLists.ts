import { ref, computed } from 'vue';
import type { TodoItem, TodoList } from '../types/todo';
import { todoApi } from '../services/todoApi';
import { getErrorMessage } from '../services/http';

const todoLists = ref<TodoList[]>([]);
const activeTodoListId = ref<string>('');

const validTodoLists = computed(() =>
  todoLists.value.filter((l): l is TodoList => l != null && typeof l === 'object' && 'id' in l),
);

const activeTodoList = computed<TodoList | null>(() => {
  return validTodoLists.value.find((l: TodoList) => l.id === activeTodoListId.value) ?? null;
});

export function useTodoLists(showAlert: (msg: string) => void) {
  async function initializeTodoLists() {
    try {
      const remoteLists = await todoApi.fetchLists(true);
      todoLists.value = (remoteLists ?? []).filter(
        (l): l is TodoList => l != null && typeof l === 'object' && 'id' in l,
      );
      activeTodoListId.value = todoLists.value[0]?.id ?? '';
    } catch (e) {
      const message = getErrorMessage(e, '加载清单失败');
      showAlert(message);
      todoLists.value = [];
      activeTodoListId.value = '';
    }
  }

  function handleUpdateTodos(nextTodos: TodoItem[]) {
    const current = todoLists.value.find((l: TodoList) => l.id === activeTodoListId.value);
    if (!current) return;
    current.todos = nextTodos;
  }

  function handleCreateTodoListPrompt(openPrompt: () => void) {
    openPrompt();
  }

  async function onTodoListPromptConfirm(name: string) {
    try {
      const newList = await todoApi.createList(name);
      todoLists.value.push(newList);
      activeTodoListId.value = newList.id;
    } catch (e) {
      showAlert(getErrorMessage(e, '创建清单失败'));
    }
  }

  function handleRenameList(list: TodoList, openPrompt: (id: string, name: string) => void) {
    openPrompt(list.id, list.name);
  }

  async function onTodoListRenameConfirm(
    listId: string,
    originName: string,
    name: string,
    clearEditing: () => void,
  ) {
    if (!listId) return;
    const list = todoLists.value.find((l) => l.id === listId);
    if (!list) return;
    if (name === originName) {
      clearEditing();
      return;
    }
    try {
      await todoApi.updateList(listId, { name });
      list.name = name;
    } catch (e) {
      showAlert(getErrorMessage(e, '重命名清单失败'));
    } finally {
      clearEditing();
    }
  }

  async function handleDeleteList(list: TodoList) {
    if (!confirm(`确定要删除清单「${list.name}」吗？`)) return;
    try {
      await todoApi.deleteList(list.id);
      todoLists.value = todoLists.value.filter((l) => l.id !== list.id);
      if (activeTodoListId.value === list.id) {
        activeTodoListId.value = todoLists.value[0]?.id ?? '';
      }
    } catch (e) {
      showAlert(getErrorMessage(e, '删除清单失败'));
    }
  }

  return {
    todoLists,
    activeTodoListId,
    validTodoLists,
    activeTodoList,
    initializeTodoLists,
    handleUpdateTodos,
    handleCreateTodoListPrompt,
    onTodoListPromptConfirm,
    handleRenameList,
    onTodoListRenameConfirm,
    handleDeleteList,
  };
}


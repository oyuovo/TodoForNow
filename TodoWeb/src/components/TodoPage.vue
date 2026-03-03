<template>
  <div class="page">
    <div class="gradient-bg"></div>
    <main class="todo-card">
      <header class="todo-header">
        <div class="todo-header-main">
          <h1 class="todo-title">Todo 清单</h1>
          <p class="todo-subtitle">记录今天要做的每一件小事</p>
        </div>
      </header>

      <section class="todo-input-area">
        <div class="input-group">
          <input
            v-model="draftTitle"
            type="text"
            class="todo-input"
            placeholder="输入待办事项，例如：完成前端页面设计..."
            @keyup.enter="handleAddTodo"
          />
          <label class="timeset-toggle" title="定时任务（每日恢复）">
            <input v-model="draftTimeset" type="checkbox" />
            <AppIcon :icon="icons.clock" :size="14" />
            <span>定时</span>
          </label>
          <button class="primary-btn primary-btn-with-icon" @click="handleAddTodo" title="添加待办">
            <AppIcon :icon="icons.add" :size="16" />
            <span>添加</span>
          </button>
        </div>
        <p v-if="inputError" class="input-error">{{ inputError }}</p>
      </section>

      <section class="todo-list-section" v-if="todos.length > 0">
        <ul class="todo-list">
          <li
            v-for="(item, index) in todos"
            :key="item.id"
            class="todo-item"
            :class="{ 'pending-remove': pendingRemoveIds.has(item.id) }"
            draggable="true"
            @dragstart="handleDragStart(index)"
            @dragover.prevent="handleDragOver(index)"
            @drop.prevent="handleDrop(index)"
            @dragend="handleDragEnd"
          >
            <label class="todo-item-main">
              <input
                type="checkbox"
                class="todo-checkbox"
                :checked="pendingRemoveIds.has(item.id)"
                @change="handleCheckboxChange(item)"
              />
              <template v-if="editingTodoId === item.id">
                <input
                  ref="editInputRef"
                  type="text"
                  class="todo-edit-input"
                  v-model="editingTitle"
                  @blur="handleEditBlur(item)"
                  @keyup.enter="handleEditConfirm(item)"
                  @keyup.escape="handleEditCancel"
                />
              </template>
              <span
                v-else
                class="todo-text"
                :class="{ 'todo-text--editable': !pendingRemoveIds.has(item.id) }"
                @dblclick="handleStartEdit(item)"
              >
                {{ item.title }}
              </span>
              <span v-if="(item.timeset ?? 0) === 1" class="todo-timeset-badge">定时</span>
              <span v-if="pendingRemoveIds.has(item.id)" class="todo-pending-hint">3 秒后移除，点击可取消</span>
            </label>
          </li>
        </ul>
      </section>

      <section class="todo-empty" v-else>
        <p>还没有任何待办事项，先添加一条吧。</p>
      </section>

      <footer class="todo-footer">
        <div class="footer-left">
          <span>总计：{{ todos.length }}</span>
          <span>已完成：{{ completedCount }}</span>
        </div>
        <div class="footer-right">
          <span v-if="operationError" class="save-message">{{ operationError }}</span>
          <button class="link-btn link-btn-with-icon" type="button" @click="handleClearCompleted" :disabled="completedCount === 0" title="清除已完成">
            <AppIcon :icon="icons.clear" :size="14" />
            <span>清除已完成</span>
          </button>
        </div>
      </footer>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import AppIcon from './AppIcon.vue';
import type { TodoItem, TodoList } from '../types/todo';
import { icons } from '../icons';
import { todoApi } from '../services/todoApi';
import { getErrorMessage } from '../services/http';

const props = withDefaults(
  defineProps<{
    list: TodoList | null;
  }>(),
  {
    list: null,
  },
);

const emit = defineEmits<{
  (e: 'update-todos', value: TodoItem[]): void;
}>();

const todos = computed<TodoItem[]>({
  get() {
    return props.list?.todos ?? [];
  },
  set(newVal: TodoItem[]) {
    emit('update-todos', newVal);
  },
});
const draftTitle = ref('');
const draftTimeset = ref(false);
const inputError = ref('');
const draggingIndex = ref<number | null>(null);
const operationError = ref('');
const pendingRemoveIds = ref<Set<string>>(new Set());
const pendingRemoveTimers = ref<Record<string, ReturnType<typeof setTimeout>>>({});
const editingTodoId = ref<string | null>(null);
const editingTitle = ref('');
const editInputRef = ref<HTMLInputElement | null>(null);

/** 已完成数量 = 处于 3 秒倒计时中的非定时任务（pendingRemoveIds） */
const completedCount = computed(() => pendingRemoveIds.value.size);

function resetInput() {
  draftTitle.value = '';
  inputError.value = '';
}

function validateInput(title: string): boolean {
  if (!title.trim()) {
    inputError.value = '请输入待办内容';
    return false;
  }
  if (title.length > 100) {
    inputError.value = '待办内容请控制在 100 字以内';
    return false;
  }
  inputError.value = '';
  return true;
}

/** 生成全局唯一 ID，避免多清单下 itemid 主键冲突（如 T1 在不同清单重复） */
function generateTodoId(): string {
  return crypto.randomUUID();
}

async function handleAddTodo() {
  if (!props.list) return;
  if (!props.list.id?.trim()) {
    inputError.value = '当前清单 ID 无效，请先选择或创建清单';
    return;
  }
  const title = draftTitle.value.trim();
  if (!validateInput(title)) return;

  const id = generateTodoId();
  const timeset = draftTimeset.value ? 1 : 0;
  const newTodo: TodoItem = {
    id,
    title,
    completed: false,
    timeset,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };

  todos.value.unshift(newTodo);
  resetInput();

  try {
    await todoApi.createTodo(props.list.id, { id, context: title, timeset });
  } catch (e) {
    todos.value = todos.value.filter((t) => t.id !== newTodo.id);
    inputError.value = getErrorMessage(e, '创建待办失败');
  }
}

function handleCheckboxChange(item: TodoItem) {
  const id = item.id;
  if (pendingRemoveIds.value.has(id)) {
    const timer = pendingRemoveTimers.value[id];
    if (timer) clearTimeout(timer);
    delete pendingRemoveTimers.value[id];
    const next = new Set(pendingRemoveIds.value);
    next.delete(id);
    pendingRemoveIds.value = next;
  } else {
    pendingRemoveIds.value = new Set(pendingRemoveIds.value).add(id);
    const timer = window.setTimeout(() => {
      const todo = todos.value.find((t: TodoItem) => t.id === id);
      if (todo?.timeset === 1) {
        doMarkScheduledComplete(id);
      } else {
        doRemove(id);
      }
      delete pendingRemoveTimers.value[id];
      const next = new Set(pendingRemoveIds.value);
      next.delete(id);
      pendingRemoveIds.value = next;
    }, 3000);
    pendingRemoveTimers.value[id] = timer;
  }
}

async function doMarkScheduledComplete(id: string) {
  if (!props.list) return;
  const index = todos.value.findIndex((t: TodoItem) => t.id === id);
  if (index === -1) return;

  const [removed] = todos.value.splice(index, 1);
  try {
    await todoApi.updateTodo(props.list.id, id, { timeset: 3 });
  } catch (e) {
    todos.value.splice(index, 0, removed);
    operationError.value = getErrorMessage(e, '标记完成失败');
    window.setTimeout(() => { operationError.value = ''; }, 3000);
  }
}

async function doRemove(id: string) {
  if (!props.list) return;
  const index = todos.value.findIndex((t: TodoItem) => t.id === id);
  if (index === -1) return;

  const [removed] = todos.value.splice(index, 1);
  try {
    await todoApi.deleteTodo(props.list.id, id);
  } catch (e) {
    todos.value.splice(index, 0, removed);
    operationError.value = getErrorMessage(e, '删除失败');
    window.setTimeout(() => { operationError.value = ''; }, 3000);
  }
}


async function handleClearCompleted() {
  if (!props.list) return;
  const toRemoveIds = Array.from(pendingRemoveIds.value);
  if (toRemoveIds.length === 0) return;

  // 清除所有倒计时
  toRemoveIds.forEach((id) => {
    const timer = pendingRemoveTimers.value[id];
    if (timer) clearTimeout(timer);
    delete pendingRemoveTimers.value[id];
  });
  pendingRemoveIds.value = new Set();

  const removed = todos.value.filter((t) => toRemoveIds.includes(t.id));
  todos.value = todos.value.filter((t) => !toRemoveIds.includes(t.id));

  try {
    await todoApi.clearCompleted(props.list.id, toRemoveIds);
  } catch (e) {
    todos.value = [...todos.value, ...removed];
    pendingRemoveIds.value = new Set(toRemoveIds);
    operationError.value = getErrorMessage(e, '清除已完成失败');
    window.setTimeout(() => { operationError.value = ''; }, 3000);
  }
}

function moveItem(oldIndex: number, newIndex: number) {
  if (oldIndex === newIndex) return;
  const updated = [...todos.value];
  const [moved] = updated.splice(oldIndex, 1);
  updated.splice(newIndex, 0, moved);
  todos.value = updated;
}

function handleDragStart(index: number) {
  draggingIndex.value = index;
}

function handleDragOver(index: number) {
  if (draggingIndex.value === null || draggingIndex.value === index) return;
  moveItem(draggingIndex.value, index);
  draggingIndex.value = index;
}

function handleDrop(_index: number) {
  // 可扩展：保存排序到后端
}

function handleDragEnd() {
  draggingIndex.value = null;
}

function handleStartEdit(item: TodoItem) {
  if (pendingRemoveIds.value.has(item.id)) return;
  editingTodoId.value = item.id;
  editingTitle.value = item.title;
  nextTick(() => editInputRef.value?.focus());
}

function handleEditCancel() {
  editingTodoId.value = null;
  editingTitle.value = '';
}

async function handleEditConfirm(item: TodoItem) {
  const trimmed = editingTitle.value.trim();
  if (!trimmed) {
    handleEditCancel();
    return;
  }
  if (trimmed === item.title) {
    handleEditCancel();
    return;
  }
  if (trimmed.length > 100) {
    operationError.value = '待办内容请控制在 100 字以内';
    window.setTimeout(() => { operationError.value = ''; }, 3000);
    return;
  }
  const prevTitle = item.title;
  item.title = trimmed;
  editingTodoId.value = null;
  editingTitle.value = '';
  try {
    await todoApi.updateTodo(props.list!.id, item.id, { context: trimmed });
  } catch (e) {
    item.title = prevTitle;
    operationError.value = getErrorMessage(e, '更新待办失败');
    window.setTimeout(() => { operationError.value = ''; }, 3000);
  }
}

function handleEditBlur(item: TodoItem) {
  if (editingTodoId.value === item.id) {
    handleEditConfirm(item);
  }
}

onBeforeUnmount(() => {
  Object.values(pendingRemoveTimers.value).forEach((t) => clearTimeout(t as ReturnType<typeof setTimeout>));
  pendingRemoveTimers.value = {};
});
</script>

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
          <button class="primary-btn" @click="handleAddTodo">
            添加
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
              <span class="todo-text">
                {{ item.title }}
              </span>
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
          <button class="link-btn" type="button" @click="handleClearCompleted" :disabled="completedCount === 0">
            清除已完成
          </button>
        </div>
      </footer>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import type { TodoItem, TodoList } from '../types/todo';
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
const inputError = ref('');
const draggingIndex = ref<number | null>(null);
const operationError = ref('');
/** 已打勾、等待 3 秒后移除的待办 id 集合 */
const pendingRemoveIds = ref<Set<string>>(new Set());
/** 每个待办的 3 秒定时器，用于取消时 clearTimeout */
const pendingRemoveTimers = ref<Record<string, ReturnType<typeof setTimeout>>>({});

const completedCount = computed(() =>
  todos.value.filter((t: TodoItem) => t.completed).length,
);

async function initializeFromBackend() {
  try {
    // 预留：后端接入时，可以在此处根据当前清单 ID 拉取数据
    // 目前清单与待办数据由上层组件管理，这里不做初始化操作
  } catch (error) {
    // 可根据需要增加错误提示
    // console.error('初始化待办失败', error);
  }
}

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

/** 生成下一个待办 ID：T1, T2, T3... 删除后复用最小可用编号（如删 T2 后下一个仍是 T2） */
function getNextTodoId(existingIds: string[]): string {
  const used = new Set(
    existingIds
      .map((id) => /^T(\d+)$/.exec(id)?.[1])
      .filter(Boolean)
      .map(Number),
  );
  let n = 1;
  while (used.has(n)) n++;
  return `T${n}`;
}

async function handleAddTodo() {
  if (!props.list) return;
  if (!props.list.id?.trim()) {
    inputError.value = '当前清单 ID 无效，请先选择或创建清单';
    return;
  }
  const title = draftTitle.value.trim();
  if (!validateInput(title)) return;

  const id = getNextTodoId(todos.value.map((t) => t.id));
  const newTodo: TodoItem = {
    id,
    title,
    completed: false,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };

  // 本地先更新 UI
  todos.value.unshift(newTodo);
  resetInput();

  try {
    await todoApi.createTodo(props.list.id, { id, context: title });
  } catch (e) {
    todos.value = todos.value.filter((t) => t.id !== newTodo.id);
    inputError.value = getErrorMessage(e, '创建待办失败');
  }
}

/** 前方案框：打勾后 3 秒移除；3 秒内再次点击可取消，方框恢复空白、待办保留 */
function handleCheckboxChange(item: TodoItem) {
  const id = item.id;
  if (pendingRemoveIds.value.has(id)) {
    // 用户反悔：取消定时器，恢复空白，待办保留
    const timer = pendingRemoveTimers.value[id];
    if (timer) clearTimeout(timer);
    delete pendingRemoveTimers.value[id];
    const next = new Set(pendingRemoveIds.value);
    next.delete(id);
    pendingRemoveIds.value = next;
  } else {
    // 用户打勾：加入待移除集合，3 秒后执行删除
    pendingRemoveIds.value = new Set(pendingRemoveIds.value).add(id);
    const timer = window.setTimeout(() => {
      doRemove(id);
      delete pendingRemoveTimers.value[id];
      const next = new Set(pendingRemoveIds.value);
      next.delete(id);
      pendingRemoveIds.value = next;
    }, 3000);
    pendingRemoveTimers.value[id] = timer;
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
  const remaining = todos.value.filter((t: TodoItem) => !t.completed);
  const removed = todos.value.filter((t: TodoItem) => t.completed);
  if (removed.length === 0) return;

  todos.value = remaining;

  try {
    await todoApi.clearCompleted(props.list.id);
  } catch (e) {
    todos.value = [...todos.value, ...removed];
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
  // 预留：这里可以调用后端接口保存排序结果，例如：
  // void todoApi.reorderTodos(todos.value.map(t => t.id));
}

function handleDragEnd() {
  draggingIndex.value = null;
}

onMounted(() => {
  void initializeFromBackend();
});

onBeforeUnmount(() => {
  Object.values(pendingRemoveTimers.value).forEach((t) => clearTimeout(t as ReturnType<typeof setTimeout>));
  pendingRemoveTimers.value = {};
});
</script>

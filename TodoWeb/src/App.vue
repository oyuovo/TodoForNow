<template>
  <div class="app-root">
    <PromptModal
      v-model="showTodoListPrompt"
      title="请输入新清单名称（不超过 20 个字符）"
      placeholder="新清单"
      defaultValue="新清单"
      @confirm="onTodoListPromptConfirm"
    />
    <PromptModal
      v-model="showMemoPrompt"
      title="请输入新备忘录标题（不超过 20 个字符）"
      placeholder="新备忘录"
      defaultValue="新备忘录"
      @confirm="onMemoPromptConfirm"
    />
    <PromptModal
      v-model="showTodoListRenamePrompt"
      title="请输入清单新名称（不超过 20 个字符）"
      placeholder="清单名称"
      :defaultValue="editingListName"
      :maxLength="20"
      @confirm="onTodoListRenameConfirm"
    />
    <AlertModal v-model="showAlert" :message="alertMessage" />
    <template v-if="isLoggedIn">
  <div class="app-shell">
    <aside class="app-sidebar">
      <div class="sidebar-time">{{ currentTime }}</div>
      <div class="sidebar-header">
        <div class="avatar-wrapper">
          <div class="avatar-circle">
            <img
              v-if="avatarImage"
              :src="avatarImage"
              alt="用户头像"
              class="avatar-img"
            />
            <span v-else class="avatar-initials">
              {{ userInitials }}
            </span>
          </div>
        </div>
        <div class="user-meta">
          <div class="user-name">{{ userName }}</div>
          <div class="user-id">ID：{{ userId }}</div>
        </div>
      </div>

      <nav class="sidebar-nav">
        <button
          type="button"
          class="nav-item"
          :class="{ active: activeView === 'profile' }"
          @click="activeView = 'profile'"
        >
          <span class="nav-dot" />
          <AppIcon :icon="icons.user" :size="16" class="nav-item-icon" />
          <span>个人信息</span>
        </button>
        <div class="nav-group">
          <button
            type="button"
            class="nav-item"
            :class="{ active: activeView === 'todo' }"
            @click="activeView = 'todo'"
          >
            <span class="nav-dot" />
            <AppIcon :icon="icons.list" :size="16" class="nav-item-icon" />
            <span>待办清单</span>
          </button>
          <div v-if="activeView === 'todo'" class="nav-sublist">
            <div
              v-for="list in validTodoLists"
              :key="list.id"
              class="nav-subitem-row"
            >
              <button
                type="button"
                class="nav-subitem"
                :class="{ active: list.id === activeTodoListId }"
                @click="activeTodoListId = list.id"
              >
                {{ list.name }}
              </button>
        <button
          type="button"
          class="nav-subitem-icon"
          title="重命名清单"
          @click.stop="handleRenameList(list)"
        >
          <AppIcon :icon="icons.edit" :size="14" />
        </button>
        <button
          type="button"
          class="nav-subitem-icon nav-subitem-delete"
          title="删除清单"
          @click.stop="handleDeleteList(list)"
        >
          <AppIcon :icon="icons.delete" :size="14" />
        </button>
            </div>
            <button
              type="button"
              class="nav-subitem nav-subitem-new"
              title="新建清单"
              @click="handleCreateTodoList"
            >
              <AppIcon :icon="icons.add" :size="14" />
              <span>新清单</span>
            </button>
          </div>
        </div>
        <div class="nav-group">
        <button
          type="button"
          class="nav-item"
          :class="{ active: activeView === 'memo' }"
          @click="activeView = 'memo'"
        >
          <span class="nav-dot" />
          <AppIcon :icon="icons.memo" :size="16" class="nav-item-icon" />
          <span>备忘录</span>
        </button>
          <div v-if="activeView === 'memo'" class="nav-sublist">
            <div
              v-for="memo in memos"
              :key="memo.id"
              class="nav-subitem-row"
            >
              <button
                type="button"
                class="nav-subitem"
                :class="{ active: memo.id === activeMemoId }"
                @click="activeMemoId = memo.id"
              >
                {{ memo.title }}
              </button>
              <button
                type="button"
                class="nav-subitem-icon nav-subitem-delete"
                title="删除备忘录"
                @click.stop="handleDeleteMemo(memo)"
              >
                <AppIcon :icon="icons.delete" :size="14" />
              </button>
            </div>
            <button
              type="button"
              class="nav-subitem nav-subitem-new"
              title="新建备忘录"
              @click="handleCreateMemo"
            >
              <AppIcon :icon="icons.add" :size="14" />
              <span>新备忘录</span>
            </button>
          </div>
        </div>
      </nav>

      <div class="sidebar-footer">
        <button
          type="button"
          class="theme-toggle"
          :title="theme.value === 'light' ? '切换为深色模式' : '切换为浅色模式'"
          @click="theme.toggle()"
        >
          <AppIcon :icon="theme.value === 'light' ? icons.moon : icons.sun" :size="18" />
        </button>
        <span class="sidebar-tip">更多功能 · 敬请期待</span>
      </div>
    </aside>

    <main class="app-main">
      <section v-if="activeView === 'profile'" class="page page-profile">
        <ProfilePage
          :user-name="userName"
          :user-id="userId"
          :avatar-url="avatarImage"
          @update-avatar="handleUpdateAvatar"
        />
      </section>
      <section v-else-if="activeView === 'todo'" class="page page-todo">
        <div v-if="validTodoLists.length === 0" class="todo-empty-state">
          <div class="todo-empty-state-card">
            <p class="todo-empty-state-text">还没有任何清单，创建你的第一个清单开始记录待办吧</p>
            <button type="button" class="todo-empty-state-btn" @click="handleCreateTodoList">
              创建第一个清单
            </button>
          </div>
        </div>
        <TodoPage
          v-else
          :list="activeTodoList"
          @update-todos="handleUpdateTodos"
        />
      </section>
      <section v-else class="page page-memo">
        <MemoPage
          :memo="activeMemo"
          :memo-base-path="memoBasePath"
          @update-content="handleUpdateMemoContent"
          @save="handleSaveMemo"
          @set-path="handleSetMemoPath"
          @open-folder="handleOpenMemoFolder"
        />
      </section>
    </main>
  </div>
    </template>
    <LoginPage v-else />
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import AppIcon from './components/AppIcon.vue';
import TodoPage from './components/TodoPage.vue';
import ProfilePage from './components/ProfilePage.vue';
import MemoPage from './components/MemoPage.vue';
import LoginPage from './components/LoginPage.vue';
import PromptModal from './components/PromptModal.vue';
import AlertModal from './components/AlertModal.vue';
import type { Memo } from './types/memo';
import type { TodoItem, TodoList } from './types/todo';
import { auth } from './services/auth';
import { theme } from './services/theme';
import { memoLocalService } from './services/memoLocalService';
import { todoApi } from './services/todoApi';
import { userApi } from './services/userApi';
import { getErrorMessage } from './services/http';
import { icons } from './icons';

const currentTime = ref('');
let timeInterval: ReturnType<typeof setInterval> | null = null;

function updateCurrentTime() {
  const now = new Date();
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });
}

const isLoggedIn = ref(!!auth.getToken());
watch(
  () => auth.token.value,
  (val) => {
    isLoggedIn.value = !!val;
  },
);

// 用户基础信息：由 /user/profile 接口返回的 data 填充（基于当前登录用户）
const userName = ref('示例用户');
const userId = ref('1');
const avatarImage = ref<string>('/avatar.png');

type ViewType = 'profile' | 'todo' | 'memo';
const activeView = ref<ViewType>('profile');

const todoLists = ref<TodoList[]>([
]);

const activeTodoListId = ref<string>('');

const memos = ref<Memo[]>([]);
const memoBasePath = ref('');
const activeMemoId = ref<string>('');
const showTodoListPrompt = ref(false);
const showMemoPrompt = ref(false);
const showTodoListRenamePrompt = ref(false);
const editingListId = ref<string>('');
const editingListName = ref('');
const showAlert = ref(false);
const alertMessage = ref('');

const userInitials = computed(() => {
  const name = userName.value.trim();
  if (!name) return 'U';
  // 简单从姓名中取前两个字符作为头像文字
  return name.slice(0, 2);
});

const validTodoLists = computed(() =>
  todoLists.value.filter((l): l is TodoList => l != null && typeof l === 'object' && 'id' in l),
);

const activeTodoList = computed<TodoList | null>(() => {
  return validTodoLists.value.find((l: TodoList) => l.id === activeTodoListId.value) ?? null;
});

const activeMemo = computed<Memo | null>(() => {
  return memos.value.find((m: Memo) => m.id === activeMemoId.value) ?? null;
});

async function initializeTodoLists() {
  try {
    const remoteLists = await todoApi.fetchLists(true);
    todoLists.value = (remoteLists ?? []).filter(
      (l): l is TodoList => l != null && typeof l === 'object' && 'id' in l,
    );
    activeTodoListId.value = todoLists.value[0]?.id ?? '';
  } catch {
    // 后端不可用时，构造一个本地默认清单
    const fallback: TodoList = {
      id: 'default-list',
      name: '默认清单',
      todos: [],
    };
    todoLists.value = [fallback];
    activeTodoListId.value = fallback.id;
  }
}

async function initializeMemos() {
  try {
    const basePath = await memoLocalService.getBasePath();
    memoBasePath.value = basePath;
    const list = await memoLocalService.loadMemos(basePath);
    memos.value = list;
    activeMemoId.value = list[0]?.id ?? '';
  } catch {
    const list = await memoLocalService.loadMemos('');
    memos.value = list;
    activeMemoId.value = list[0]?.id ?? '';
  }
}

async function initializeUserProfile() {
  try {
    const data = await userApi.getProfile();
    userName.value = data.username;
    userId.value = String(data.id);
    avatarImage.value = data.photo || '/avatar.png';
  } catch {
    // 后端不可用时保留默认占位
  }
}

function handleUpdateAvatar(url: string) {
  avatarImage.value = url || '/avatar.png';
}

function handleCreateMemo() {
  showMemoPrompt.value = true;
}

async function onMemoPromptConfirm(name: string) {
  try {
    const newMemo = await memoLocalService.createMemo(memoBasePath.value, name);
    memos.value = await memoLocalService.loadMemos(memoBasePath.value);
    activeMemoId.value = newMemo.id;
  } catch (e) {
    alertMessage.value = getErrorMessage(e, '创建备忘录失败');
    showAlert.value = true;
  }
}

function handleUpdateMemoContent(nextContent: string) {
  const current = memos.value.find((m: Memo) => m.id === activeMemoId.value);
  if (!current) return;
  current.content = nextContent;
  current.updatedAt = new Date().toISOString();
}

async function handleSaveMemo() {
  const current = memos.value.find((m: Memo) => m.id === activeMemoId.value);
  if (!current) return;
  try {
    await memoLocalService.saveMemo(memoBasePath.value, current);
  } catch (e) {
    alertMessage.value = getErrorMessage(e, '保存备忘录失败');
    showAlert.value = true;
  }
}

async function handleSetMemoPath(path: string) {
  try {
    await memoLocalService.setBasePath(path);
    memoBasePath.value = path;
    const list = await memoLocalService.loadMemos(path);
    memos.value = list;
    activeMemoId.value = list[0]?.id ?? '';
  } catch (e) {
    alertMessage.value = getErrorMessage(e, '设置路径失败');
    showAlert.value = true;
  }
}

async function handleOpenMemoFolder() {
  try {
    await memoLocalService.openFolderInExplorer(memoBasePath.value);
  } catch (e) {
    alertMessage.value = getErrorMessage(e, '打开文件夹失败');
    showAlert.value = true;
  }
}

async function handleDeleteMemo(memo: Memo) {
  if (!confirm(`确定要删除备忘录「${memo.title}」吗？此操作将删除本地文件，无法恢复。`)) return;
  try {
    await memoLocalService.deleteMemo(memoBasePath.value, memo.id);
    memos.value = memos.value.filter((m) => m.id !== memo.id);
    if (activeMemoId.value === memo.id) {
      activeMemoId.value = memos.value[0]?.id ?? '';
    }
  } catch (e) {
    alertMessage.value = getErrorMessage(e, '删除备忘录失败');
    showAlert.value = true;
  }
}

function handleCreateTodoList() {
  showTodoListPrompt.value = true;
}

function handleRenameList(list: TodoList) {
  editingListId.value = list.id;
  editingListName.value = list.name;
  showTodoListRenamePrompt.value = true;
}

async function onTodoListRenameConfirm(name: string) {
  const listId = editingListId.value;
  if (!listId) return;
  const list = todoLists.value.find((l) => l.id === listId);
  if (!list) return;
  if (name === list.name) {
    editingListId.value = '';
    editingListName.value = '';
    return;
  }
  try {
    await todoApi.updateList(listId, { name });
    list.name = name;
  } catch (e) {
    alertMessage.value = getErrorMessage(e, '重命名清单失败');
    showAlert.value = true;
  } finally {
    editingListId.value = '';
    editingListName.value = '';
  }
}

async function onTodoListPromptConfirm(name: string) {
  try {
    const newList = await todoApi.createList(name);
    const toAdd =
      newList != null && typeof newList === 'object' && newList.id != null
        ? newList
        : { id: crypto.randomUUID(), name, todos: [] as TodoItem[] };
    todoLists.value.push(toAdd);
    activeTodoListId.value = toAdd.id;
  } catch (e) {
    alertMessage.value = getErrorMessage(e, '创建清单失败');
    showAlert.value = true;
  }
}

function handleUpdateTodos(nextTodos: TodoItem[]) {
  const current = todoLists.value.find((l: TodoList) => l.id === activeTodoListId.value);
  if (!current) return;
  current.todos = nextTodos;
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
    alertMessage.value = getErrorMessage(e, '删除清单失败');
    showAlert.value = true;
  }
}

// 桌面环境下，切换备忘录时从本地文件加载内容
watch(
  [activeMemoId, memoBasePath],
  async ([id, basePath]: [string, string]) => {
    if (!id || !basePath || typeof window === 'undefined' || !window.memoFs) return;
    const current = memos.value.find((m: Memo) => m.id === id);
    if (!current) return;
    try {
      const content = await memoLocalService.readMemoContent(basePath, id, current);
      memos.value = memos.value.map((m: Memo) =>
        m.id === id ? { ...m, content } : m,
      );
    } catch {
      // 忽略单条读取失败
    }
  },
);

watch(
  isLoggedIn,
  (loggedIn: boolean) => {
    if (loggedIn) {
      void initializeTodoLists();
      void initializeMemos();
      void initializeUserProfile();
    }
  },
  { immediate: true },
);

onMounted(() => {
  updateCurrentTime();
  timeInterval = setInterval(updateCurrentTime, 1000);
});

onBeforeUnmount(() => {
  if (timeInterval) clearInterval(timeInterval);
});
</script>

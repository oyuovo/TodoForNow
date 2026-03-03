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
        <button
          type="button"
          class="nav-item"
          :class="{ active: activeView === 'stats' }"
          @click="activeView = 'stats'"
        >
          <span class="nav-dot" />
          <AppIcon :icon="icons.list" :size="16" class="nav-item-icon" />
          <span>统计面板</span>
        </button>
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
      <section v-else-if="activeView === 'memo'" class="page page-memo">
        <MemoPage
          :memo="activeMemo"
          :memo-base-path="memoBasePath"
          @update-content="handleUpdateMemoContent"
          @save="handleSaveMemo"
          @set-path="handleSetMemoPath"
          @open-folder="handleOpenMemoFolder"
        />
      </section>
      <section v-else class="page page-stats">
        <StatsPage />
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
import StatsPage from './components/StatsPage.vue';
import LoginPage from './components/LoginPage.vue';
import PromptModal from './components/PromptModal.vue';
import AlertModal from './components/AlertModal.vue';
import type { TodoItem } from './types/todo';
import { auth } from './services/auth';
import { theme } from './services/theme';
import { icons } from './icons';
import { useUserProfile } from './composables/useUserProfile';
import { useTodoLists } from './composables/useTodoLists';
import { useMemos } from './composables/useMemos';
import { showDailyTodoSummaryOncePerDay } from './services/notifications';

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

type ViewType = 'profile' | 'todo' | 'memo' | 'stats';
const activeView = ref<ViewType>('profile');
const showTodoListPrompt = ref(false);
const showMemoPrompt = ref(false);
const showTodoListRenamePrompt = ref(false);
const editingListId = ref<string>('');
const editingListName = ref('');
const showAlert = ref(false);
const alertMessage = ref('');

function openAlert(message: string) {
  alertMessage.value = message;
  showAlert.value = true;
}

const {
  userName,
  userId,
  avatarImage,
  userInitials,
  initializeUserProfile,
  handleUpdateAvatar,
} = useUserProfile();

const {
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
} = useTodoLists(openAlert);

const {
  memos,
  memoBasePath,
  activeMemoId,
  activeMemo,
  initializeMemos,
  onMemoPromptConfirm,
  handleUpdateMemoContent,
  handleSaveMemo,
  handleSetMemoPath,
  handleOpenMemoFolder,
  handleDeleteMemo,
} = useMemos(openAlert);

function handleCreateMemo() {
  showMemoPrompt.value = true;
}

watch(
  isLoggedIn,
  (loggedIn: boolean) => {
    if (loggedIn) {
      void (async () => {
        await initializeTodoLists();
        try {
          const allTodos = todoLists.value
            .map((l) => l.todos ?? [])
            .reduce((acc, cur) => acc.concat(cur), [] as TodoItem[]);
          const scheduledCount = allTodos.filter((t) => (t.timeset ?? 0) === 1).length;
          const normalCount = allTodos.filter((t) => (t.timeset ?? 0) === 0).length;
          await showDailyTodoSummaryOncePerDay({ scheduledCount, normalCount });
        } catch {
          // 通知失败不影响主流程
        }
      })();
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

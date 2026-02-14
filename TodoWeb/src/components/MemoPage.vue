<template>
  <PromptModal
    v-model="showPathPrompt"
    title="请输入备忘录根路径（本地 .md 文件存放目录）"
    :placeholder="pathPlaceholder"
    :defaultValue="defaultPathValue"
    :maxLength="512"
    @confirm="onPathConfirm"
  />
  <div class="memo-card" v-if="memo" :class="{ 'memo-card--browse': isBrowseMode }">
    <header class="memo-header">
      <div class="memo-header-main">
        <h1 class="memo-title">{{ memo.title }}</h1>
        <p class="memo-subtitle" v-if="!isBrowseMode">以 Markdown 形式记下你的想法与灵感</p>
      </div>
      <div class="memo-header-meta" v-if="!isBrowseMode">
        <span class="memo-meta-label">存储路径：{{ memoBasePath || '未设置（示例：C:\\Users\\用户名\\Documents\\memos\\）' }}</span>
      </div>
    </header>

    <!-- 编辑模式：左侧编辑 + 右侧预览 -->
    <section v-if="!isBrowseMode" class="memo-main">
      <div class="memo-editor">
        <textarea
          class="memo-textarea"
          :value="memo.content"
          placeholder="使用 Markdown 语法书写，例如：&#10;&#10;# 今天的计划&#10;- [ ] 完成前端页面布局&#10;- [ ] 编写接口联调文档"
          @input="onInput"
        />
      </div>
      <div class="memo-preview">
        <div class="memo-preview-header">预览</div>
        <div
          class="memo-preview-body"
          v-html="renderedMarkdown"
        />
      </div>
    </section>

    <!-- 浏览模式：纯阅读，整块渲染后的 Markdown -->
    <section v-else class="memo-browse">
      <div class="memo-browse-body" v-html="renderedMarkdown" />
    </section>

    <footer class="memo-footer">
      <span v-if="saveMessage" class="memo-save-message">{{ saveMessage }}</span>
      <button v-if="!isBrowseMode" type="button" class="memo-save-btn" @click="handleSave">
        保存
      </button>
      <button
        type="button"
        :class="['memo-mode-btn', isBrowseMode ? 'memo-mode-btn--primary' : 'memo-mode-btn--secondary']"
        @click="isBrowseMode = !isBrowseMode"
      >
        {{ isBrowseMode ? '编辑' : '浏览' }}
      </button>
      <button
        type="button"
        class="memo-mode-btn memo-mode-btn--secondary memo-path-btn"
        @click="handleSetPath"
      >
        设置路径
      </button>
      <button
        type="button"
        class="memo-mode-btn memo-mode-btn--secondary"
        @click="handleOpenFolder"
        title="在文件管理器中打开当前备忘录所在文件夹"
      >
        打开文件夹
      </button>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { marked } from 'marked';
import PromptModal from './PromptModal.vue';
import type { Memo } from '../types/memo';

interface Props {
  memo: Memo | null;
  memoBasePath?: string;
}

const props = withDefaults(
  defineProps<Props>(),
  { memoBasePath: '' },
);

const emit = defineEmits<{
  (e: 'update-content', value: string): void;
  (e: 'save'): void;
  (e: 'set-path', path: string): void;
  (e: 'open-folder'): void;
}>();

const renderedMarkdown = computed(() => {
  if (!props.memo) return '';
  return marked.parse(props.memo.content ?? '');
});

const saveMessage = ref('');
const isBrowseMode = ref(false);
const showPathPrompt = ref(false);

const PATH_EXAMPLE_WIN = 'C:\\Users\\YourName\\Documents\\memos\\';
const pathPlaceholder = 'C:\\Users\\用户名\\Documents\\memos\\';
const defaultPathValue = computed(() => props.memoBasePath || PATH_EXAMPLE_WIN);

function onInput(event: Event) {
  const target = event.target as HTMLTextAreaElement;
  emit('update-content', target.value);
}

function handleSave() {
  emit('save');
  saveMessage.value = '已保存（当前仅本地状态，后端待接入）';
}

function handleSetPath() {
  showPathPrompt.value = true;
}

function onPathConfirm(path: string) {
  emit('set-path', path);
}

function handleOpenFolder() {
  emit('open-folder');
}

watch(
  () => props.memo?.content,
  () => {
    // 内容变更时清空旧的保存提示
    saveMessage.value = '';
  },
);
</script>


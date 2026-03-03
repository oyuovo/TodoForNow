import { ref, computed, watch } from 'vue';
import type { Memo } from '../types/memo';
import { memoLocalService } from '../services/memoLocalService';
import { getErrorMessage } from '../services/http';

const memos = ref<Memo[]>([]);
const memoBasePath = ref('');
const activeMemoId = ref<string>('');

const activeMemo = computed<Memo | null>(() => {
  return memos.value.find((m: Memo) => m.id === activeMemoId.value) ?? null;
});

export function useMemos(showAlert: (msg: string) => void) {
  async function initializeMemos() {
    try {
      const basePath = await memoLocalService.getBasePath();
      memoBasePath.value = basePath;
      const list = await memoLocalService.loadMemos(basePath);
      memos.value = list;
      activeMemoId.value = list[0]?.id ?? '';
    } catch (e) {
      showAlert(getErrorMessage(e, '加载备忘录失败'));
      const list = await memoLocalService.loadMemos('');
      memos.value = list;
      activeMemoId.value = list[0]?.id ?? '';
    }
  }

  function handleCreateMemo(openPrompt: () => void) {
    openPrompt();
  }

  async function onMemoPromptConfirm(name: string) {
    try {
      const newMemo = await memoLocalService.createMemo(memoBasePath.value, name);
      memos.value = await memoLocalService.loadMemos(memoBasePath.value);
      activeMemoId.value = newMemo.id;
    } catch (e) {
      showAlert(getErrorMessage(e, '创建备忘录失败'));
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
      showAlert(getErrorMessage(e, '保存备忘录失败'));
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
      showAlert(getErrorMessage(e, '设置路径失败'));
    }
  }

  async function handleOpenMemoFolder() {
    try {
      await memoLocalService.openFolderInExplorer(memoBasePath.value);
    } catch (e) {
      showAlert(getErrorMessage(e, '打开文件夹失败'));
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
      showAlert(getErrorMessage(e, '删除备忘录失败'));
    }
  }

  // 桌面环境下切换备忘录时，从本地文件重新加载内容
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

  return {
    memos,
    memoBasePath,
    activeMemoId,
    activeMemo,
    initializeMemos,
    handleCreateMemo,
    onMemoPromptConfirm,
    handleUpdateMemoContent,
    handleSaveMemo,
    handleSetMemoPath,
    handleOpenMemoFolder,
    handleDeleteMemo,
  };
}


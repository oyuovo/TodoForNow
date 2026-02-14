/** 备忘录本地服务：桌面环境下读写 .md 文件，浏览器下用内存兜底 */
import type { Memo } from '../types/memo';
import { memoPathApi } from './memoPathApi';

export interface MemoFsBridge {
  list(basePath: string): Promise<{ id: string; title: string }[]>;
  read(basePath: string, id: string): Promise<string>;
  write(basePath: string, id: string, content: string): Promise<void>;
  create(basePath: string, title: string): Promise<{ id: string }>;
  delete(basePath: string, id: string): Promise<void>;
  openFolder(folderPath: string): Promise<string>;
}

declare global {
  interface Window {
    memoFs?: MemoFsBridge;
  }
}

const now = () => new Date().toISOString();

let fallbackMemos: Memo[] = [
  {
    id: 'default-memo',
    title: '默认备忘录',
    content: '',
    createdAt: now(),
    updatedAt: now(),
  },
];

function isDesktop(): boolean {
  return typeof window !== 'undefined' && typeof window.memoFs === 'object';
}

function toMemo(item: { id: string; title: string }, content = ''): Memo {
  return {
    id: item.id,
    title: item.title,
    content,
    createdAt: now(),
    updatedAt: now(),
  };
}

export const memoLocalService = {
  async getBasePath(): Promise<string> {
    try {
      return await memoPathApi.getMemoPath();
    } catch {
      return '';
    }
  },

  async setBasePath(path: string): Promise<void> {
    await memoPathApi.setMemoPath(path);
  },

  async loadMemos(basePath: string): Promise<Memo[]> {
    if (basePath && isDesktop() && window.memoFs) {
      try {
        const list = await window.memoFs.list(basePath);
        return list.map((item) => toMemo(item));
      } catch {
        return [...fallbackMemos];
      }
    }
    return [...fallbackMemos];
  },

  async readMemoContent(basePath: string, id: string, currentMemo: Memo | null): Promise<string> {
    if (basePath && isDesktop() && window.memoFs) {
      try {
        return await window.memoFs.read(basePath, id);
      } catch {
        return currentMemo?.content ?? '';
      }
    }
    return currentMemo?.content ?? '';
  },

  async saveMemo(basePath: string, memo: Memo): Promise<void> {
    if (basePath && isDesktop() && window.memoFs) {
      await window.memoFs.write(basePath, memo.id, memo.content);
      return;
    }
    const idx = fallbackMemos.findIndex((m) => m.id === memo.id);
    if (idx >= 0) {
      fallbackMemos[idx] = { ...memo, updatedAt: now() };
    } else {
      fallbackMemos.push({ ...memo, updatedAt: now() });
    }
  },

  async createMemo(basePath: string, title: string): Promise<Memo> {
    if (basePath && isDesktop() && window.memoFs) {
      const { id } = await window.memoFs.create(basePath, title);
      return toMemo({ id, title }, '');
    }
    const newMemo: Memo = {
      id: crypto.randomUUID(),
      title,
      content: '',
      createdAt: now(),
      updatedAt: now(),
    };
    fallbackMemos.push(newMemo);
    return newMemo;
  },

  async deleteMemo(basePath: string, memoId: string): Promise<void> {
    if (basePath && isDesktop() && window.memoFs) {
      await window.memoFs.delete(basePath, memoId);
      return;
    }
    fallbackMemos = fallbackMemos.filter((m) => m.id !== memoId);
  },

  async openFolderInExplorer(folderPath: string): Promise<void> {
    if (!folderPath) throw new Error('请先设置备忘录路径');
    if (!isDesktop() || !window.memoFs) throw new Error('打开文件夹功能仅在桌面版可用');
    const openFolder = window.memoFs.openFolder;
    if (typeof openFolder !== 'function') {
      throw new Error('当前版本不支持打开文件夹，请完全退出并重新启动 Electron 应用');
    }
    const err = await openFolder(folderPath);
    if (err) throw new Error(err || '无法打开文件夹');
  },
};

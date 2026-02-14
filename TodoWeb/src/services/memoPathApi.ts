/** 备忘录根路径配置，桌面应用据此读写本地 .md 文件 */
export interface MemoPathSetting {
  memopath: string;
}

const PATH_API = '/settings/memo-path';

export const memoPathApi = {
  async getMemoPath(): Promise<string> {
    const { httpRequestUnwrap } = await import('./http');
    const raw = await httpRequestUnwrap<MemoPathSetting | string>(PATH_API, { method: 'GET' });
    if (raw && typeof raw === 'object' && 'memopath' in raw) {
      return (raw as MemoPathSetting).memopath ?? '';
    }
    // 后端可能把 data 包装成 JSON 字符串 "{ \"memopath\": \"...\" }"，需解析后提取路径
    if (typeof raw === 'string') {
      try {
        const parsed = JSON.parse(raw) as MemoPathSetting;
        if (parsed && typeof parsed === 'object' && 'memopath' in parsed) {
          return parsed.memopath ?? '';
        }
      } catch {
        // 非 JSON 字符串时视为纯路径
      }
      return raw;
    }
    return '';
  },

  async setMemoPath(path: string): Promise<void> {
    const { httpRequestUnwrap } = await import('./http');
    await httpRequestUnwrap<void>(PATH_API, {
      method: 'POST',
      body: JSON.stringify({ memopath: path }),
    });
  },
};

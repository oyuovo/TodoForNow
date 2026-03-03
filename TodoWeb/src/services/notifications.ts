const NOTIFICATION_FLAG_KEY = 'justtodo:last-summary-date';

function isNotificationSupported(): boolean {
  return typeof window !== 'undefined' && 'Notification' in window;
}

async function ensurePermission(): Promise<boolean> {
  if (!isNotificationSupported()) return false;
  if (Notification.permission === 'granted') return true;
  if (Notification.permission === 'denied') return false;
  const result = await Notification.requestPermission();
  return result === 'granted';
}

export async function showNotification(title: string, body: string): Promise<void> {
  const ok = await ensurePermission();
  if (!ok) return;
  // 在 Electron 渲染进程中使用浏览器 Notification 即可触发系统通知
  new Notification(title, { body });
}

function getTodayKey(): string {
  const now = new Date();
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

export async function showDailyTodoSummaryOncePerDay(payload: {
  scheduledCount: number;
  normalCount: number;
}): Promise<void> {
  const today = getTodayKey();
  try {
    const stored = window.localStorage.getItem(NOTIFICATION_FLAG_KEY);
    if (stored === today) return;
  } catch {
    // 忽略本地存储异常
  }

  const { scheduledCount, normalCount } = payload;
  if (scheduledCount <= 0 && normalCount <= 0) {
    return;
  }

  const lines: string[] = [];
  if (scheduledCount > 0) {
    lines.push(`定时任务：${scheduledCount} 条`);
  }
  if (normalCount > 0) {
    lines.push(`普通待办：${normalCount} 条`);
  }
  const body = lines.join('\n');

  await showNotification('今日待办概览', body);

  try {
    window.localStorage.setItem(NOTIFICATION_FLAG_KEY, today);
  } catch {
    // 忽略本地存储异常
  }
}


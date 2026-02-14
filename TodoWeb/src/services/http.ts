const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';
const API_PREFIX = '/api';

/** 后端 ResultDTO.fail(errorMsg) 统一格式 */
export interface HttpErrorBody {
  code?: string;
  message?: string;
  errorMsg?: string;
  errorCode?: string;
  /** 业务失败时 data 可能为错误信息字符串 */
  data?: unknown;
  details?: unknown;
}

/** 后端 { success, errorCode, data } 业务失败时的错误信息提取 */
export function extractBackendErrorMessage(res: {
  success?: boolean;
  errorCode?: string;
  data?: unknown;
  errorMsg?: string;
}): string {
  if (typeof res.data === 'string' && res.data) return res.data;
  if (res.errorMsg) return res.errorMsg;
  if (res.errorCode) return res.errorCode;
  return '操作失败，请稍后重试';
}

export class HttpError extends Error {
  status: number;
  body: HttpErrorBody | null;

  constructor(status: number, body: HttpErrorBody | null, message?: string) {
    const msg = message ?? body?.errorMsg ?? body?.message ?? `HTTP error ${status}`;
    super(msg);
    this.status = status;
    this.body = body;
  }
}

/** 从任意异常中提取用户可读的错误信息，优先使用后端 data/errorMsg */
export function getErrorMessage(e: unknown, fallback = '操作失败，请稍后重试'): string {
  if (e instanceof HttpError) {
    const b = e.body;
    return (
      b?.errorMsg ??
      (typeof b?.data === 'string' ? b.data : null) ??
      b?.message ??
      e.message ??
      fallback
    );
  }
  if (e instanceof Error) {
    return e.message || fallback;
  }
  return fallback;
}

async function parseJsonSafe(response: Response): Promise<any | null> {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

export async function httpRequest<T>(
  path: string,
  options: RequestInit & { skipJson?: boolean; skipAuth?: boolean } = {},
): Promise<T> {
  const { skipJson, skipAuth, headers, ...rest } = options;

  const { auth } = await import('./auth');
  const token = auth.getToken();
  const useAuth = !skipAuth && token && !auth.isDevSkipToken(token);
  const authHeader: Record<string, string> =
    useAuth ? { Authorization: `Bearer ${token}` } : {};

  const url = `${BASE_URL}${API_PREFIX}${path}`;
  const resp = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...authHeader,
      ...(headers ?? {}),
    },
    ...rest,
  });

  if (!resp.ok) {
    const { isDevSkipLogin } = await import('./auth');
    if ((resp.status === 401 || resp.status === 403) && !isDevSkipLogin) {
      auth.clearToken();
    }
    const body = (await parseJsonSafe(resp)) as HttpErrorBody | null;
    throw new HttpError(resp.status, body);
  }

  if (resp.status === 204 || skipJson) {
    // @ts-expect-error allow void
    return undefined;
  }

  const data = (await parseJsonSafe(resp)) as T;
  return data;
}

export interface BackendResponse<T> {
  success?: boolean;
  errorCode?: string;
  data: T;
}

/** 若响应为 { success, errorCode, data } 则返回 data，否则返回原响应；业务失败时抛出并携带 data 错误信息 */
export async function httpRequestUnwrap<T>(
  path: string,
  options: RequestInit & { skipJson?: boolean; skipAuth?: boolean } = {},
): Promise<T> {
  const raw = await httpRequest<BackendResponse<T> | T>(path, options);
  if (raw != null && typeof raw === 'object' && 'success' in raw) {
    const res = raw as BackendResponse<T> & { success?: boolean };
    if (res.success === false) {
      throw new HttpError(0, {
        errorMsg: extractBackendErrorMessage(res),
        errorCode: res.errorCode,
        data: res.data,
      });
    }
  }
  if (raw != null && typeof raw === 'object' && 'data' in raw) {
    return (raw as BackendResponse<T>).data as T;
  }
  return raw as T;
}
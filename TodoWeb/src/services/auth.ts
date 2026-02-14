import { ref } from 'vue';
import { httpRequest } from './http';

const TOKEN_KEY = 'auth_token';
const USER_ID_KEY = 'auth_user_id';

/** 开发模式跳过登录，便于单独调试前端 */
export const isDevSkipLogin =
  import.meta.env.DEV && import.meta.env.VITE_DEV_SKIP_LOGIN === 'true';

const DEV_SKIP_PLACEHOLDER = '__dev_skip_login__';

const tokenRef = ref<string | null>(
  isDevSkipLogin ? DEV_SKIP_PLACEHOLDER : localStorage.getItem(TOKEN_KEY),
);

const currentUserIdRef = ref<number | null>(
  (() => {
    const stored = localStorage.getItem(USER_ID_KEY);
    return stored != null ? parseInt(stored, 10) : null;
  })(),
);

export const auth = {
  token: tokenRef,

  getToken(): string | null {
    return tokenRef.value;
  },

  isDevSkipToken(token: string | null): boolean {
    return token === DEV_SKIP_PLACEHOLDER;
  },

  setToken(t: string | null) {
    if (t) {
      tokenRef.value = t;
      if (t !== DEV_SKIP_PLACEHOLDER) {
        localStorage.setItem(TOKEN_KEY, t);
      }
    } else {
      if (isDevSkipLogin) {
        tokenRef.value = DEV_SKIP_PLACEHOLDER;
        return;
      }
      tokenRef.value = null;
      localStorage.removeItem(TOKEN_KEY);
      currentUserIdRef.value = null;
      localStorage.removeItem(USER_ID_KEY);
    }
  },

  clearToken() {
    if (isDevSkipLogin) {
      tokenRef.value = DEV_SKIP_PLACEHOLDER;
      return;
    }
    tokenRef.value = null;
    localStorage.removeItem(TOKEN_KEY);
    currentUserIdRef.value = null;
    localStorage.removeItem(USER_ID_KEY);
  },

  /** 当前登录用户 id，登录成功后设置，登出时清除 */
  currentUserId: currentUserIdRef,

  getCurrentUserId(): number | null {
    return currentUserIdRef.value;
  },

  setCurrentUserId(id: number | null) {
    currentUserIdRef.value = id;
    if (id != null) {
      localStorage.setItem(USER_ID_KEY, String(id));
    } else {
      localStorage.removeItem(USER_ID_KEY);
    }
  },
};

export interface LoginBody {
  id: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  errorCode: string;
  /** token 字符串，或 { token, id } 等扩展格式 */
  data: string | { token?: string; id?: number };
}

export async function login(id: string, password: string): Promise<LoginResponse> {
  const body: LoginBody = { id, password };
  return httpRequest<LoginResponse>('/user/login', {
    method: 'POST',
    body: JSON.stringify(body),
    skipAuth: true,
  });
}

export interface LogoutResponse {
  success: boolean;
  errorCode: string;
  data?: unknown;
}

export async function logout(): Promise<void> {
  try {
    await httpRequest<LogoutResponse>('/user/logout', {
      method: 'POST',
    });
  } catch {
    // 无论后端是否成功，都清除本地 token
  } finally {
    auth.clearToken();
  }
}

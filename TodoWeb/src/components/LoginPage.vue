<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-title">登录</h1>
      <p class="login-desc">请输入账号与密码</p>
      <form class="login-form" @submit.prevent="handleSubmit">
        <label class="login-label">
          <span>ID</span>
          <input
            v-model="id"
            type="text"
            class="login-input"
            placeholder="用户 ID"
            required
            autocomplete="username"
          />
        </label>
        <label class="login-label">
          <span>密码</span>
          <input
            v-model="password"
            type="password"
            class="login-input"
            placeholder="密码"
            required
            autocomplete="current-password"
          />
        </label>
        <p v-if="errorMessage" class="login-error">{{ errorMessage }}</p>
        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? '登录中…' : '登录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { auth, login } from '../services/auth';
import { extractBackendErrorMessage, getErrorMessage } from '../services/http';

const id = ref('');
const password = ref('');
const errorMessage = ref('');
const loading = ref(false);

async function handleSubmit() {
  errorMessage.value = '';
  loading.value = true;
  try {
    const res = await login(id.value.trim(), password.value);
    // 后端返回 success: false 时（如 200 + 业务失败），不应视为登录成功
    if (!res.success) {
      throw new Error(extractBackendErrorMessage(res));
    }
    const data = res.data;
    const token =
      typeof data === 'string' ? data : (data as { token?: string }).token;
    const userIdFromRes =
      typeof data === 'object' && data != null && 'id' in data
        ? (data as { id?: number }).id
        : undefined;
    if (token) {
      auth.setToken(token);
      // 使用登录表单的 id 或响应中的 id 作为当前用户 id
      const uid = userIdFromRes ?? parseInt(id.value.trim(), 10);
      if (!Number.isNaN(uid)) auth.setCurrentUserId(uid);
    }
  } catch (e: unknown) {
    errorMessage.value = getErrorMessage(e, '登录失败，请检查 ID 与密码');
  } finally {
    loading.value = false;
  }
}
</script>

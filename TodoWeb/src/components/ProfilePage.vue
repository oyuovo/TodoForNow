<template>
  <div class="profile-card">
    <header class="profile-header">
      <div class="profile-header-main">
        <h1 class="profile-title">个人主页</h1>
        <p class="profile-subtitle">概览你的基础信息与账号标识</p>
      </div>
      <button
        type="button"
        class="profile-logout-btn"
        :disabled="loading"
        @click="handleLogout"
      >
        {{ loading ? '退出中…' : '退出登录' }}
      </button>
    </header>

    <section class="profile-main">
      <div class="profile-avatar-block">
        <div class="profile-avatar">
          <img
            v-if="displayAvatarUrl"
            :src="displayAvatarUrl"
            alt="用户头像"
            class="profile-avatar-img"
            @error="onAvatarError"
          />
          <span v-else class="profile-avatar-text">
            {{ initials }}
          </span>
        </div>
        <div class="profile-avatar-edit">
          <input
            v-model="avatarUrlInput"
            type="text"
            class="profile-avatar-input"
            placeholder="输入头像图片的在线地址（http/https）"
            maxlength="500"
          />
          <button
            type="button"
            class="profile-avatar-save-btn"
            :disabled="saving"
            @click="handleSaveAvatar"
          >
            {{ saving ? '保存中…' : '保存' }}
          </button>
          <p v-if="avatarError" class="profile-avatar-error">{{ avatarError }}</p>
          <p v-else class="profile-avatar-tip">
            请输入图片所在的在线地址，保存后头像将更新。
          </p>
        </div>
      </div>

      <div class="profile-info-block">
        <div class="profile-field">
          <div class="profile-label">姓名 / 名称</div>
          <div class="profile-value readonly">{{ userName }}</div>
        </div>

        <div class="profile-field">
          <div class="profile-label">用户 ID</div>
          <div class="profile-value readonly">{{ userId }}</div>
        </div>

        <div class="profile-hint">
          当前名称与 ID 由系统生成，暂不支持在此界面修改。
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { logout } from '../services/auth';
import { userApi } from '../services/userApi';
import { getErrorMessage } from '../services/http';

interface Props {
  userName: string;
  userId: string;
  avatarUrl?: string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: 'update-avatar', url: string): void;
}>();

const loading = ref(false);
const saving = ref(false);
const avatarUrlInput = ref(props.avatarUrl ?? '');
const avatarError = ref('');
const avatarLoadFailed = ref(false);

watch(
  () => props.avatarUrl,
  (url) => {
    avatarUrlInput.value = url ?? '';
    avatarLoadFailed.value = false;
  },
);

const displayAvatarUrl = computed(() => {
  if (avatarLoadFailed.value || !props.avatarUrl?.trim()) return '';
  return props.avatarUrl;
});

const initials = computed(() => {
  const name = props.userName.trim();
  if (!name) return 'U';
  return name.slice(0, 2);
});

function onAvatarError() {
  avatarLoadFailed.value = true;
}

function isValidUrl(url: string): boolean {
  const trimmed = url.trim();
  return trimmed === '' || /^https?:\/\/.+/i.test(trimmed);
}

async function handleSaveAvatar() {
  avatarError.value = '';
  const url = avatarUrlInput.value.trim();
  if (!isValidUrl(url)) {
    avatarError.value = '请输入有效的图片地址（以 http:// 或 https:// 开头）';
    return;
  }
  saving.value = true;
  try {
    await userApi.updatePhoto(url);
    avatarLoadFailed.value = false;
    emit('update-avatar', url);
  } catch (e) {
    avatarError.value = getErrorMessage(e, '保存头像失败');
  } finally {
    saving.value = false;
  }
}

async function handleLogout() {
  loading.value = true;
  try {
    await logout();
  } finally {
    loading.value = false;
  }
}
</script>


import { ref, computed } from 'vue';
import type { UserProfileData } from '../services/userApi';
import { userApi } from '../services/userApi';

const userName = ref('示例用户');
const userId = ref('1');
const avatarImage = ref<string>('/avatar.png');

const userInitials = computed(() => {
  const name = userName.value.trim();
  if (!name) return 'U';
  return name.slice(0, 2);
});

export function useUserProfile() {
  async function initializeUserProfile() {
    try {
      const data = await userApi.getProfile();
      userName.value = data.username;
      userId.value = String(data.id);
      avatarImage.value = data.photo || '/avatar.png';
    } catch {
      // 保持占位数据
    }
  }

  function handleUpdateAvatar(url: string) {
    avatarImage.value = url || '/avatar.png';
  }

  return {
    userName,
    userId,
    avatarImage,
    userInitials,
    initializeUserProfile,
    handleUpdateAvatar,
  };
}


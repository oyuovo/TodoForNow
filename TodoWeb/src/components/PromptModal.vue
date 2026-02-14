<template>
  <Teleport to="body">
    <div v-if="modelValue" class="prompt-overlay" @click.self="handleCancel">
      <div class="prompt-modal">
        <p class="prompt-title">{{ title }}</p>
        <input
          ref="inputRef"
          v-model="localValue"
          type="text"
          class="prompt-input"
          :placeholder="placeholder"
          :maxlength="maxLength && maxLength > 0 ? maxLength : undefined"
          @keyup.enter="handleConfirm"
          @keyup.escape="handleCancel"
        />
        <p v-if="error" class="prompt-error">{{ error }}</p>
        <div class="prompt-actions">
          <button type="button" class="prompt-btn prompt-btn-cancel" @click="handleCancel">
            取消
          </button>
          <button type="button" class="prompt-btn prompt-btn-confirm" @click="handleConfirm">
            确定
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue';

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    title: string;
    placeholder?: string;
    defaultValue?: string;
    maxLength?: number;
  }>(),
  {
    placeholder: '',
    defaultValue: '',
    maxLength: 20,
  },
);

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void;
  (e: 'confirm', value: string): void;
  (e: 'cancel'): void;
}>();

const inputRef = ref<HTMLInputElement | null>(null);
const localValue = ref('');
const error = ref('');

watch(
  () => props.modelValue,
  (show) => {
    if (show) {
      localValue.value = props.defaultValue;
      error.value = '';
      nextTick(() => inputRef.value?.focus());
    }
  },
);

function handleConfirm() {
  const trimmed = localValue.value.trim();
  if (!trimmed) {
    error.value = '请输入内容';
    return;
  }
  const max = props.maxLength ?? 20;
  if (max > 0 && trimmed.length > max) {
    error.value = `请控制在 ${max} 个字符以内`;
    return;
  }
  emit('confirm', trimmed);
  emit('update:modelValue', false);
}

function handleCancel() {
  emit('cancel');
  emit('update:modelValue', false);
}
</script>

<style scoped>
.prompt-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.prompt-modal {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  min-width: 320px;
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.2);
}

.prompt-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.prompt-input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}

.prompt-input:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 1px rgba(99, 102, 241, 0.3);
}

.prompt-error {
  margin: 8px 0 0;
  font-size: 12px;
  color: #dc2626;
}

.prompt-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.prompt-btn {
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
}

.prompt-btn-cancel {
  background: #f3f4f6;
  color: #374151;
}

.prompt-btn-cancel:hover {
  background: #e5e7eb;
}

.prompt-btn-confirm {
  background: #111827;
  color: #ffffff;
}

.prompt-btn-confirm:hover {
  background: #374151;
}
</style>

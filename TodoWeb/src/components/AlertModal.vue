<template>
  <Teleport to="body">
    <div v-if="modelValue" class="alert-overlay" @click.self="handleClose">
      <div class="alert-modal">
        <p class="alert-message">{{ message }}</p>
        <button type="button" class="alert-btn" @click="handleClose">确定</button>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: boolean;
  message: string;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void;
}>();

function handleClose() {
  emit('update:modelValue', false);
}
</script>

<style scoped>
.alert-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.alert-modal {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  min-width: 280px;
  max-width: 400px;
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.2);
}

.alert-message {
  margin: 0 0 20px;
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
}

.alert-btn {
  display: block;
  width: 100%;
  padding: 10px;
  border-radius: 8px;
  border: none;
  background: #111827;
  color: #ffffff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.alert-btn:hover {
  background: #374151;
}
</style>

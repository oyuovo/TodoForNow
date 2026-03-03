<template>
  <div class="page page-stats">
    <div class="stats-card">
      <header class="stats-header">
        <h1 class="stats-title">统计面板</h1>
        <p class="stats-subtitle">看看最近的任务概况与定时任务完成情况</p>
      </header>

      <section v-if="loading" class="stats-loading">
        加载中...
      </section>

      <section v-else-if="error" class="stats-error">
        {{ error }}
      </section>

      <section v-else class="stats-grid">
        <div class="stats-item">
          <div class="stats-label">清单总数</div>
          <div class="stats-value">{{ stats.totalLists }}</div>
        </div>
        <div class="stats-item">
          <div class="stats-label">待办总数</div>
          <div class="stats-value">{{ stats.totalTodos }}</div>
        </div>
        <div class="stats-item">
          <div class="stats-label">普通待办</div>
          <div class="stats-value">{{ stats.normalTotal }}</div>
        </div>
        <div class="stats-item">
          <div class="stats-label">定时任务总数</div>
          <div class="stats-value">{{ stats.scheduledTotal }}</div>
        </div>
        <div class="stats-item">
          <div class="stats-label">定时任务已完成</div>
          <div class="stats-value">{{ stats.scheduledCompleted }}</div>
        </div>
        <div class="stats-item">
          <div class="stats-label">定时任务完成率</div>
          <div class="stats-value">
            <span v-if="scheduledRate !== null">{{ scheduledRate }}%</span>
            <span v-else>-</span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { todoApi } from '../services/todoApi';
import { getErrorMessage } from '../services/http';

interface TodoStats {
  totalLists: number;
  totalTodos: number;
  scheduledTotal: number;
  scheduledCompleted: number;
  normalTotal: number;
}

const loading = ref(false);
const error = ref('');
const stats = ref<TodoStats>({
  totalLists: 0,
  totalTodos: 0,
  scheduledTotal: 0,
  scheduledCompleted: 0,
  normalTotal: 0,
});

const scheduledRate = computed<number | null>(() => {
  if (!stats.value.scheduledTotal) return null;
  const rate =
    (stats.value.scheduledCompleted / stats.value.scheduledTotal) * 100;
  return Math.round(rate);
});

async function loadStats() {
  loading.value = true;
  error.value = '';
  try {
    const data = await todoApi.fetchStats();
    stats.value = data;
  } catch (e) {
    error.value = getErrorMessage(e, '加载统计数据失败');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadStats();
});
</script>


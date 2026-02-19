<template>
  <Icon :icon="icon" :width="size" :height="size" :class="iconClass" />
</template>

<script setup lang="ts">
import { Icon } from '@iconify/vue';

withDefaults(
  defineProps<{
    icon: string;
    size?: number | string;
    iconClass?: string;
  }>(),
  {
    size: 18,
    iconClass: '',
  },
);
</script>

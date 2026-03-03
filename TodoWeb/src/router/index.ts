import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router';
import App from '../App.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'root',
    component: App,
  },
];

export const router = createRouter({
  history: createWebHashHistory(),
  routes,
});


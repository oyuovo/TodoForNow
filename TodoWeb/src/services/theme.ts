import { ref, watch } from 'vue';

const STORAGE_KEY = 'justtodo_theme';

export type Theme = 'light' | 'dark';

function loadTheme(): Theme {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'light' || stored === 'dark') return stored;
  } catch {
    /* ignore */
  }
  return 'light';
}

const themeRef = ref<Theme>(loadTheme());

watch(
  themeRef,
  (val) => {
    try {
      localStorage.setItem(STORAGE_KEY, val);
    } catch {
      /* ignore */
    }
    document.documentElement.setAttribute('data-theme', val);
  },
  { immediate: true },
);

export const theme = {
  value: themeRef,
  isDark: () => themeRef.value === 'dark',
  isLight: () => themeRef.value === 'light',
  setTheme(t: Theme) {
    themeRef.value = t;
  },
  toggle() {
    themeRef.value = themeRef.value === 'light' ? 'dark' : 'light';
  },
};

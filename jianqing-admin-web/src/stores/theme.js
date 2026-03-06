import { reactive } from 'vue';

const THEME_KEY = 'jq_theme';

export const THEMES = [
  { key: 'midnight', label: '午夜蓝' },
  { key: 'light', label: '晴空白' },
  { key: 'emerald', label: '青岚绿' },
  { key: 'sunset', label: '暮光橙' },
  { key: 'violet', label: '星幕紫' }
];

const state = reactive({
  current: localStorage.getItem(THEME_KEY) || 'midnight'
});

function applyDomTheme(themeKey) {
  document.documentElement.setAttribute('data-theme', themeKey);
}

export const themeStore = {
  get current() {
    return state.current;
  },
  init() {
    applyDomTheme(state.current);
  },
  setTheme(themeKey) {
    state.current = themeKey;
    localStorage.setItem(THEME_KEY, themeKey);
    applyDomTheme(themeKey);
  }
};

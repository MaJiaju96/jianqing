import { createApp } from 'vue';
import 'element-plus/dist/index.css';
import './styles/base.css';
import App from './App.vue';
import { router } from './router/index.js';
import { themeStore } from './stores/theme.js';

themeStore.init();

createApp(App).use(router).mount('#app');

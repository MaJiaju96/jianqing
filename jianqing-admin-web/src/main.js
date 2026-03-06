import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import 'element-plus/dist/index.css';
import './styles/base.css';
import App from './App.vue';
import { router } from './router/index.js';
import { themeStore } from './stores/theme.js';

themeStore.init();

createApp(App).use(ElementPlus, { locale: zhCn }).use(router).mount('#app');

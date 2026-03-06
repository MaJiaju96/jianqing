import axios from 'axios';
import { ElMessage } from 'element-plus';
import { API_SUCCESS_CODE } from '../constants/app';
import { authStore } from '../stores/auth';

export const http = axios.create({
  baseURL: '/api',
  timeout: 10000
});

http.interceptors.request.use((config) => {
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`;
  }
  return config;
});

http.interceptors.response.use(
  (res) => {
    const payload = res.data;
    if (payload?.code !== API_SUCCESS_CODE) {
      ElMessage.error(payload?.message ?? '请求失败');
      return Promise.reject(new Error(payload?.message ?? '请求失败'));
    }
    return payload.data;
  },
  (error) => {
    const status = error?.response?.status;
    if (status === 401) {
      authStore.logout();
      location.hash = '#/login';
    }
    ElMessage.error(error?.response?.data?.message ?? error?.message ?? '服务异常');
    return Promise.reject(error);
  }
);

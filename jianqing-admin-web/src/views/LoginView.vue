<template>
  <div class="jq-login-wrap">
    <div class="jq-grid-bg"></div>
    <el-card class="jq-login-card jq-glass-card">
      <h2 class="jq-login-title">欢迎来到简擎</h2>
      <p class="jq-login-subtitle">轻量、可插拔、工程化友好的管理中台</p>
      <el-form :model="form" @keyup.enter="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" show-password placeholder="密码" size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="jq-login-btn" :loading="loading" @click="handleLogin">
            进入管理台
          </el-button>
        </el-form-item>
      </el-form>
      <div class="jq-tip">默认账号：admin / 你初始化 SQL 中设置的密码</div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { fetchProfile, login } from '../api/auth';
import { authStore } from '../stores/auth';

const router = useRouter();
const loading = ref(false);
const form = reactive({
  username: 'admin',
  password: 'admin123'
});

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }
  loading.value = true;
  try {
    const result = await login(form.username, form.password);
    authStore.setToken(result.accessToken);
    const profile = await fetchProfile();
    authStore.setProfile(profile);
    await router.replace('/dashboard');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.jq-login-wrap {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.jq-grid-bg {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(140, 167, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(140, 167, 255, 0.08) 1px, transparent 1px);
  background-size: 40px 40px;
  transform: perspective(600px) rotateX(60deg) scale(1.4);
  opacity: 0.65;
}

.jq-login-card {
  width: 440px;
  z-index: 2;
  border: none;
}

.jq-login-title {
  margin: 0;
  font-size: 30px;
}

.jq-login-subtitle {
  margin: 8px 0 24px;
  color: var(--jq-login-subtitle);
}

.jq-login-btn {
  width: 100%;
  border-radius: 12px;
}

.jq-tip {
  font-size: 12px;
  color: var(--jq-login-tip);
}
</style>

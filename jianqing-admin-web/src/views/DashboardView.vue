<template>
  <div>
    <h2 class="jq-page-title">控制台总览</h2>
    <div class="jq-page-subtitle">欢迎回来，{{ profileName }}。这是简擎的实时视图。</div>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="8">
        <el-card class="jq-glass-card stat-card">
          <div class="label">用户规模</div>
          <div class="value">{{ usersCountDisplay }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="jq-glass-card stat-card">
          <div class="label">角色数量</div>
          <div class="value">{{ rolesCountDisplay }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="jq-glass-card stat-card">
          <div class="label">菜单节点</div>
          <div class="value">{{ menuCountDisplay }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { fetchRoles, fetchUsers, fetchMenuTree } from '../api/system';
import { authStore } from '../stores/auth';
import { hasPerm } from '../utils/permission';
import { ignoreHandledError } from '../utils/errors';

const usersCount = ref(null);
const rolesCount = ref(null);
const menuCount = ref(null);

const profileName = computed(() => authStore.profile?.nickname || authStore.profile?.username || '管理员');
const usersCountDisplay = computed(() => (usersCount.value == null ? '--' : usersCount.value));
const rolesCountDisplay = computed(() => (rolesCount.value == null ? '--' : rolesCount.value));
const menuCountDisplay = computed(() => (menuCount.value == null ? '--' : menuCount.value));

function countMenus(tree) {
  return tree.reduce((total, item) => total + 1 + countMenus(item.children || []), 0);
}

onMounted(async () => {
  try {
    const requests = [];
    if (hasPerm('system:user:list')) {
      requests.push(fetchUsers().then((users) => {
        usersCount.value = users.length;
      }));
    }
    if (hasPerm('system:role:list')) {
      requests.push(fetchRoles().then((roles) => {
        rolesCount.value = roles.length;
      }));
    }
    if (hasPerm('system:menu:list')) {
      requests.push(fetchMenuTree().then((menus) => {
        menuCount.value = countMenus(menus);
      }));
    }
    await Promise.all(requests);
  } catch (error) {
    ignoreHandledError(error);
  }
});
</script>

<style scoped>
.stat-card {
  border: none;
}

.label {
  color: var(--jq-card-muted-text);
  font-size: 14px;
}

.value {
  margin-top: 10px;
  font-size: 34px;
  font-weight: 700;
  color: var(--jq-card-strong-text);
}
</style>

<template>
  <div>
    <h2 class="jq-page-title">控制台总览</h2>
    <div class="jq-page-subtitle">欢迎回来，{{ profileName }}。这是简擎的实时视图。</div>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="8">
        <el-card class="jq-glass-card stat-card">
          <div class="label">用户规模</div>
          <div class="value">{{ usersCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="jq-glass-card stat-card">
          <div class="label">角色数量</div>
          <div class="value">{{ rolesCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="jq-glass-card stat-card">
          <div class="label">菜单节点</div>
          <div class="value">{{ menuCount }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { fetchRoles, fetchUsers, fetchMenuTree } from '../api/system';
import { authStore } from '../stores/auth';

const usersCount = ref(0);
const rolesCount = ref(0);
const menuCount = ref(0);

const profileName = computed(() => authStore.profile?.nickname || authStore.profile?.username || '管理员');

function countMenus(tree) {
  return tree.reduce((total, item) => total + 1 + countMenus(item.children || []), 0);
}

onMounted(async () => {
  const [users, roles, menus] = await Promise.all([fetchUsers(), fetchRoles(), fetchMenuTree()]);
  usersCount.value = users.length;
  rolesCount.value = roles.length;
  menuCount.value = countMenus(menus);
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

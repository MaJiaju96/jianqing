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
import { computed } from 'vue';
import { useOverviewCounts } from '../composables/useOverviewCounts';
import { usePageInitializer } from '../composables/usePageInitializer';
import { authStore } from '../stores/auth';

const profileName = computed(() => authStore.profile?.nickname || authStore.profile?.username || '管理员');
const { usersCountDisplay, rolesCountDisplay, menuCountDisplay, loadCounts } = useOverviewCounts();

usePageInitializer(loadCounts);
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

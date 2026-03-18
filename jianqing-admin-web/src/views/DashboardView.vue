<template>
  <div>
    <h2 class="jq-page-title">控制台总览</h2>
    <div class="jq-page-subtitle">欢迎回来，{{ profileName }}。这是简擎的实时视图。</div>

    <el-dialog v-model="noticeDialogVisible" title="待处理通知" width="620px" append-to-body>
      <div class="dashboard-notice-list">
        <div v-for="item in popupCandidates" :key="item.noticeId" class="dashboard-notice-item" @click="openNotice(item.noticeId)">
          <div class="dashboard-notice-item__title">{{ item.title }}</div>
          <div class="dashboard-notice-item__meta">
            <el-tag size="small" :type="levelTagType(item.level)">{{ levelText(item.level) }}</el-tag>
            <span>{{ item.publishedAt || '刚刚发布' }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="noticeDialogVisible = false">稍后查看</el-button>
        <el-button type="primary" @click="openMessageCenter">进入消息中心</el-button>
      </template>
    </el-dialog>

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
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchMyPopupCandidates } from '../api/system';
import { NOTICE_LEVEL_OPTIONS } from '../constants/app';
import { useOverviewCounts } from '../composables/useOverviewCounts';
import { usePageInitializer } from '../composables/usePageInitializer';
import { authStore } from '../stores/auth';
import { ignoreHandledError } from '../utils/errors';

const router = useRouter();
const profileName = computed(() => authStore.profile?.nickname || authStore.profile?.username || '管理员');
const { usersCountDisplay, rolesCountDisplay, menuCountDisplay, loadCounts } = useOverviewCounts();
const popupCandidates = ref([]);
const noticeDialogVisible = ref(false);

function levelText(level) {
  return NOTICE_LEVEL_OPTIONS.find((item) => item.value === level)?.label || level || '-';
}

function levelTagType(level) {
  return NOTICE_LEVEL_OPTIONS.find((item) => item.value === level)?.tagType || 'info';
}

async function loadDashboard() {
  await loadCounts();
  if (sessionStorage.getItem('jq_notice_popup_seen') === '1') {
    return;
  }
  try {
    popupCandidates.value = await fetchMyPopupCandidates(3);
    if (popupCandidates.value.length) {
      noticeDialogVisible.value = true;
      sessionStorage.setItem('jq_notice_popup_seen', '1');
    }
  } catch (error) {
    ignoreHandledError(error);
  }
}

function openNotice(noticeId) {
  noticeDialogVisible.value = false;
  router.push(`/messages/mine/${noticeId}`);
}

function openMessageCenter() {
  noticeDialogVisible.value = false;
  router.push('/messages/mine');
}

usePageInitializer(loadDashboard);
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

.dashboard-notice-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dashboard-notice-item {
  padding: 14px 16px;
  border-radius: 16px;
  cursor: pointer;
  background: linear-gradient(135deg, rgba(217, 238, 245, 0.75), rgba(247, 244, 230, 0.8));
}

.dashboard-notice-item__title {
  font-weight: 700;
  color: var(--jq-card-title);
}

.dashboard-notice-item__meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  color: var(--jq-card-subtitle);
  font-size: 12px;
}
</style>

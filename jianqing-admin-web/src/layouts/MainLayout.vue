<template>
  <el-container class="jq-shell">
    <el-aside width="248px" class="jq-aside">
      <div class="jq-logo">简擎 · Admin</div>
      <el-menu :default-active="activePath" router class="jq-menu">
        <el-menu-item index="/dashboard">仪表盘</el-menu-item>
        <el-sub-menu v-if="showSystemMenu" index="/system">
          <template #title>系统管理</template>
          <el-menu-item v-if="canViewUsers" index="/system/users">用户管理</el-menu-item>
          <el-menu-item v-if="canViewDepts" index="/system/depts">部门管理</el-menu-item>
          <el-menu-item v-if="canViewRoles" index="/system/roles">角色管理</el-menu-item>
          <el-menu-item v-if="canViewMenus" index="/system/menus">菜单管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="showSettingsMenu" index="/settings">
          <template #title>参数管理</template>
          <el-menu-item v-if="canViewConfigs" index="/system/config">参数设置</el-menu-item>
          <el-menu-item v-if="canViewDicts" index="/system/dicts">字典管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="showDevMenu" index="/dev-tools">
          <template #title>开发工具</template>
          <el-menu-item v-if="canViewGenerator" index="/system/generator">代码生成</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="showMessageCenterMenu" index="/messages">
          <template #title>消息中心</template>
          <el-menu-item index="/messages/mine">我的消息</el-menu-item>
          <el-menu-item v-if="canViewNoticeManage" index="/messages/manage">通知管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="showAuditMenu" index="/audit">
          <template #title>审计日志</template>
          <el-menu-item v-if="canViewOperLogs" index="/audit/oper-logs">操作日志</el-menu-item>
          <el-menu-item v-if="canViewLoginLogs" index="/audit/login-logs">登录日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="jq-header jq-glass-card">
        <div>
          <div class="jq-user">{{ nickname }}</div>
          <div class="jq-role">{{ rolesText }}</div>
        </div>
        <div class="jq-header-actions">
          <el-dropdown trigger="click" @visible-change="handleNoticeVisibleChange">
            <el-badge :value="unreadCount" :hidden="!unreadCount" class="notice-badge">
              <el-button circle :icon="Bell" />
            </el-badge>
            <template #dropdown>
              <el-dropdown-menu class="notice-dropdown-menu">
                <div class="notice-dropdown-head">
                  <span>最新消息</span>
                  <el-button text size="small" @click="router.push('/messages/mine')">查看全部</el-button>
                </div>
                <div v-if="latestNotices.length" class="notice-dropdown-list">
                  <div v-for="item in latestNotices" :key="item.noticeId" class="notice-dropdown-item" @click="openNotice(item.noticeId)">
                    <div class="notice-dropdown-item__top">
                      <span class="notice-dropdown-item__title">{{ item.title }}</span>
                      <el-tag size="small" :type="levelTagType(item.level)">{{ levelText(item.level) }}</el-tag>
                    </div>
                    <div class="notice-dropdown-item__content">{{ previewContent(item.content, 56) }}</div>
                    <div class="notice-dropdown-item__meta">
                      <span>{{ formatDateTimeText(item.publishedAt, '刚刚发布') }}</span>
                      <span v-if="item.readStatus === 0" class="notice-dropdown-item__dot">未读</span>
                    </div>
                  </div>
                </div>
                <div v-else class="notice-dropdown-empty">暂无消息</div>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown trigger="click" @command="handleThemeChange">
            <el-button plain>主题：{{ currentThemeLabel }}</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="theme in themes"
                  :key="theme.key"
                  :command="theme.key"
                  :class="{ 'is-current': theme.key === currentTheme }"
                >
                  {{ theme.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button type="primary" plain @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="jq-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <el-dialog v-model="noticeDialogVisible" title="待处理通知" width="620px" append-to-body :before-close="handlePopupBeforeClose">
    <div v-if="popupCurrent" class="global-notice-panel">
      <div class="global-notice-panel__summary">
        <span>待提醒 {{ popupTotal }} 条</span>
        <span>未读总计 {{ unreadCount }} 条</span>
      </div>
      <div class="global-notice-item" @click="openPopupNotice(popupCurrent.noticeId)">
        <div class="global-notice-item__top">
          <el-tag size="small" :type="levelTagType(popupCurrent.level)">{{ levelText(popupCurrent.level) }}</el-tag>
          <span class="global-notice-item__page">{{ popupIndex + 1 }}/{{ popupTotal }}</span>
        </div>
        <div class="global-notice-item__title">{{ popupCurrent.title }}</div>
        <div class="global-notice-item__content">{{ previewContent(popupCurrent.content, 180) }}</div>
        <div class="global-notice-item__meta">
          <span>{{ formatDateTimeText(popupCurrent.publishedAt, '刚刚发布') }}</span>
          <span v-if="popupCurrent.readStatus === 0" class="global-notice-item__dot">未读</span>
        </div>
      </div>
    </div>
    <div v-else class="notice-dropdown-empty">暂无待处理通知</div>
    <template #footer>
      <el-button :disabled="popupActionLoading || popupTotal <= 1" @click="handlePopupPrev">上一个</el-button>
      <el-button :disabled="popupActionLoading || popupTotal <= 1" @click="handlePopupNext">下一个</el-button>
      <el-button :disabled="popupActionLoading || !popupCurrent" @click="handlePopupReadAll">设置已读</el-button>
      <el-button :disabled="popupActionLoading" @click="handlePopupSnooze">五分钟内不再弹窗</el-button>
      <el-button type="primary" :disabled="popupActionLoading" @click="openMessageCenter">进入消息中心</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { Bell } from '@element-plus/icons-vue';
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { logout } from '../api/auth';
import {
  fetchMyLatestNotices,
  fetchMyNoticeRealtime,
  fetchMyNoticeUnreadCount,
  fetchMyPopupCandidates,
  markMyNoticeRead,
  subscribeMyNoticeStream
} from '../api/system';
import { NOTICE_LEVEL_OPTIONS } from '../constants/app';
import { useActionLoading } from '../composables/useAsyncState';
import { authStore } from '../stores/auth';
import { THEMES, themeStore } from '../stores/theme.js';
import { usePermissionGroup } from '../composables/usePermissions';
import { formatDateTimeText } from '../utils/datetime';
import { ignoreHandledError } from '../utils/errors';
import { showSuccessMessage } from '../utils/feedback';

const NOTICE_SNOOZE_KEY = 'jq_notice_popup_muted_until';
const NOTICE_STREAM_RETRY_DELAY = 3000;

const route = useRoute();
const router = useRouter();

const latestNotices = ref([]);
const unreadCount = ref(0);
const popupCandidates = ref([]);
const popupIndex = ref(0);
const noticeDialogVisible = ref(false);
const popupAction = useActionLoading();
let noticeStreamController = null;
let noticeStreamRetryTimer = null;
let popupSyncToken = 0;
const popupTotal = computed(() => popupCandidates.value.length);
const popupCurrent = computed(() => popupCandidates.value[popupIndex.value] || null);
const activePath = computed(() => {
  if (route.path.startsWith('/messages/mine')) {
    return '/messages/mine';
  }
  if (route.path.startsWith('/messages/manage')) {
    return '/messages/manage';
  }
  return route.path;
});
const nickname = computed(() => authStore.profile?.nickname || authStore.profile?.username || '未登录');
const rolesText = computed(() => (authStore.profile?.roles || []).join(' / ') || '暂无角色');
const themes = THEMES;
const currentTheme = computed(() => themeStore.current);
const currentThemeLabel = computed(() => {
  const current = themes.find((theme) => theme.key === currentTheme.value);
  return current ? current.label : '午夜蓝';
});
const {
  canViewUsers,
  canViewDepts,
  canViewConfigs,
  canViewDicts,
  canViewGenerator,
  canViewNoticeManage,
  canViewRoles,
  canViewMenus,
  canViewOperLogs,
  canViewLoginLogs
} = usePermissionGroup({
  canViewUsers: 'system:user:list',
  canViewDepts: 'system:dept:list',
  canViewConfigs: 'system:config:list',
  canViewDicts: 'system:dict:list',
  canViewGenerator: 'system:generator:list',
  canViewNoticeManage: 'system:notice:list',
  canViewRoles: 'system:role:list',
  canViewMenus: 'system:menu:list',
  canViewOperLogs: 'audit:oper-log:list',
  canViewLoginLogs: 'audit:login-log:list'
});
const showSettingsMenu = computed(() => canViewConfigs.value || canViewDicts.value);
const showDevMenu = computed(() => canViewGenerator.value);
const showMessageCenterMenu = computed(() => Boolean(authStore.profile));
const showSystemMenu = computed(() => canViewUsers.value || canViewDepts.value || canViewRoles.value || canViewMenus.value);
const showAuditMenu = computed(() => canViewOperLogs.value || canViewLoginLogs.value);
const popupActionLoading = popupAction.loading;

function handleThemeChange(themeKey) {
  themeStore.setTheme(themeKey);
}

async function handleLogout() {
  try {
    await logout();
    showSuccessMessage('退出登录');
  } finally {
    authStore.logout();
    router.replace('/login');
  }
}

function levelText(level) {
  return NOTICE_LEVEL_OPTIONS.find((item) => item.value === level)?.label || level || '-';
}

function levelTagType(level) {
  return NOTICE_LEVEL_OPTIONS.find((item) => item.value === level)?.tagType || 'info';
}

async function loadNoticePanel() {
  if (!authStore.isLoggedIn()) {
    unreadCount.value = 0;
    latestNotices.value = [];
    return;
  }
  try {
    const [countResult, latestResult] = await Promise.allSettled([fetchMyNoticeUnreadCount(), fetchMyLatestNotices(5)]);
    if (countResult.status === 'fulfilled') {
      unreadCount.value = Number(countResult.value || 0);
    } else {
      unreadCount.value = 0;
      ignoreHandledError(countResult.reason);
    }
    if (latestResult.status === 'fulfilled') {
      latestNotices.value = latestResult.value || [];
    } else {
      latestNotices.value = [];
      ignoreHandledError(latestResult.reason);
    }
  } catch (error) {
    ignoreHandledError(error);
  }
}

async function loadNoticeRealtimeFallback() {
  if (!authStore.isLoggedIn()) {
    unreadCount.value = 0;
    latestNotices.value = [];
    return;
  }
  try {
    const summary = await fetchMyNoticeRealtime();
    unreadCount.value = Number(summary?.unreadCount || 0);
    latestNotices.value = summary?.latestNotices || [];
  } catch (error) {
    ignoreHandledError(error);
  }
}

function popupMuted() {
  const mutedUntil = Number(sessionStorage.getItem(NOTICE_SNOOZE_KEY) || 0);
  return mutedUntil > Date.now();
}

async function tryShowPopupNotices() {
  if (!authStore.isLoggedIn() || popupMuted()) {
    resetPopupDialog();
    return;
  }
  if (route.path.startsWith('/messages/')) {
    resetPopupDialog();
    return;
  }
  const currentToken = ++popupSyncToken;
  try {
    const [candidateResult, realtimeResult] = await Promise.allSettled([fetchMyPopupCandidates(20), fetchMyNoticeRealtime()]);
    if (currentToken !== popupSyncToken) {
      return;
    }
    if (candidateResult.status === 'fulfilled') {
      popupCandidates.value = candidateResult.value || [];
      popupIndex.value = 0;
      noticeDialogVisible.value = popupCandidates.value.length > 0;
    } else {
      resetPopupDialog();
      ignoreHandledError(candidateResult.reason);
    }
    if (realtimeResult.status === 'fulfilled') {
      unreadCount.value = Number(realtimeResult.value?.unreadCount || 0);
      latestNotices.value = realtimeResult.value?.latestNotices || [];
    } else {
      unreadCount.value = 0;
      latestNotices.value = [];
      ignoreHandledError(realtimeResult.reason);
    }
  } catch (error) {
    ignoreHandledError(error);
  }
}

function handleNoticeRefreshEvent() {
  loadNoticeRealtimeFallback();
  tryShowPopupNotices();
}

async function syncPopupCandidates() {
  if (!authStore.isLoggedIn() || popupMuted()) {
    resetPopupDialog();
    return;
  }
  if (route.path.startsWith('/messages/')) {
    resetPopupDialog();
    return;
  }
  const currentToken = ++popupSyncToken;
  try {
    const candidates = await fetchMyPopupCandidates(20);
    if (currentToken !== popupSyncToken) {
      return;
    }
    popupCandidates.value = candidates || [];
    if (!popupCandidates.value.length) {
      resetPopupDialog();
      return;
    }
    popupIndex.value = Math.min(popupIndex.value, popupCandidates.value.length - 1);
    noticeDialogVisible.value = true;
  } catch (error) {
    ignoreHandledError(error);
  }
}

function previewContent(content, maxLength = 72) {
  if (!content) {
    return '-';
  }
  const normalized = String(content).replace(/\s+/g, ' ').trim();
  if (!normalized) {
    return '-';
  }
  return normalized.length > maxLength ? `${normalized.slice(0, maxLength)}...` : normalized;
}

function resetPopupDialog() {
  popupSyncToken += 1;
  noticeDialogVisible.value = false;
  popupCandidates.value = [];
  popupIndex.value = 0;
}

function handleNoticeVisibleChange(visible) {
  if (visible) {
    loadNoticePanel();
  }
}

function scheduleNoticeStreamReconnect() {
  if (noticeStreamRetryTimer || !authStore.isLoggedIn()) {
    return;
  }
  noticeStreamRetryTimer = window.setTimeout(() => {
    noticeStreamRetryTimer = null;
    connectNoticeStream();
  }, NOTICE_STREAM_RETRY_DELAY);
}

function cleanupNoticeStream() {
  if (noticeStreamController) {
    noticeStreamController.abort();
    noticeStreamController = null;
  }
  if (noticeStreamRetryTimer) {
    window.clearTimeout(noticeStreamRetryTimer);
    noticeStreamRetryTimer = null;
  }
}

function connectNoticeStream() {
  cleanupNoticeStream();
  if (!authStore.isLoggedIn()) {
    unreadCount.value = 0;
    latestNotices.value = [];
    resetPopupDialog();
    return;
  }
  noticeStreamController = new AbortController();
  subscribeMyNoticeStream(
    (payload) => {
      unreadCount.value = Number(payload?.unreadCount || 0);
      latestNotices.value = payload?.latestNotices || [];
      syncPopupCandidates();
    },
    () => {
      loadNoticeRealtimeFallback();
    },
    (error) => {
      ignoreHandledError(error);
    },
    noticeStreamController.signal
  ).then(() => {
    if (!noticeStreamController?.signal.aborted) {
      scheduleNoticeStreamReconnect();
    }
  }).catch((error) => {
    if (noticeStreamController?.signal.aborted) {
      return;
    }
    ignoreHandledError(error);
    scheduleNoticeStreamReconnect();
  });
}

function openNotice(noticeId) {
  router.push(`/messages/mine/${noticeId}`);
}

function openPopupNotice(noticeId) {
  sessionStorage.setItem(NOTICE_SNOOZE_KEY, String(Date.now() + 10 * 1000));
  resetPopupDialog();
  router.push(`/messages/mine/${noticeId}`);
}

function handlePopupPrev() {
  if (popupIndex.value <= 0) {
    return;
  }
  popupIndex.value -= 1;
}

function handlePopupNext() {
  if (popupIndex.value >= popupTotal.value - 1) {
    return;
  }
  popupIndex.value += 1;
}

async function handlePopupReadAll() {
  const current = popupCurrent.value;
  if (!current?.noticeId) {
    resetPopupDialog();
    return;
  }
  try {
    await popupAction.run(async () => {
      await markMyNoticeRead(current.noticeId);
      popupCandidates.value = popupCandidates.value.filter((item) => item.noticeId !== current.noticeId);
      if (!popupCandidates.value.length) {
        resetPopupDialog();
        showSuccessMessage('已将当前消息标记为已读');
        await loadNoticeRealtimeFallback();
        return;
      }
      if (popupIndex.value >= popupCandidates.value.length) {
        popupIndex.value = popupCandidates.value.length - 1;
      }
      showSuccessMessage('已将当前消息标记为已读');
      await loadNoticeRealtimeFallback();
    });
  } catch (error) {
    ignoreHandledError(error);
  }
}

function handlePopupSnooze() {
  sessionStorage.setItem(NOTICE_SNOOZE_KEY, String(Date.now() + 5 * 60 * 1000));
  resetPopupDialog();
  showSuccessMessage('五分钟内不再弹窗');
}

function openMessageCenter() {
  sessionStorage.setItem(NOTICE_SNOOZE_KEY, String(Date.now() + 10 * 1000));
  resetPopupDialog();
  router.push('/messages/mine');
}

function handlePopupBeforeClose(done) {
  sessionStorage.setItem(NOTICE_SNOOZE_KEY, String(Date.now() + 5 * 60 * 1000));
  resetPopupDialog();
  done();
}

watch(() => route.fullPath, () => {
  tryShowPopupNotices();
}, { immediate: true });

watch(() => authStore.token, () => {
  connectNoticeStream();
}, { immediate: true });

onMounted(() => {
  window.addEventListener('jq-notice-refresh', handleNoticeRefreshEvent);
});

onBeforeUnmount(() => {
  window.removeEventListener('jq-notice-refresh', handleNoticeRefreshEvent);
  cleanupNoticeStream();
});
</script>

<style scoped>
.jq-aside {
  margin: 12px;
  border-radius: 20px;
  background: linear-gradient(175deg, var(--jq-sidebar-bg-start), var(--jq-sidebar-bg-end));
  border: 1px solid var(--jq-sidebar-border);
}

.jq-logo {
  padding: 24px 20px;
  color: var(--jq-sidebar-logo-color);
  font-size: 22px;
  font-weight: 700;
}

.jq-menu {
  border-right: none;
  background: transparent;
}

:deep(.el-menu) {
  border-right: none;
  background: transparent;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: var(--jq-menu-text-color);
  margin: 4px 10px;
  border-radius: 10px;
  transition: all 0.2s ease;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  color: var(--jq-menu-hover-text-color);
  background: var(--jq-menu-hover-bg);
}

:deep(.el-menu-item.is-active) {
  color: var(--jq-menu-active-text-color) !important;
  font-weight: 600;
  background: linear-gradient(135deg, var(--jq-menu-active-bg-start), var(--jq-menu-active-bg-end)) !important;
  box-shadow: 0 6px 20px var(--jq-menu-active-shadow);
}

:deep(.el-menu-item.is-active:hover) {
  color: var(--jq-menu-active-text-color) !important;
  background: linear-gradient(135deg, var(--jq-menu-active-bg-start), var(--jq-menu-active-bg-end)) !important;
}

:deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: var(--jq-menu-parent-active-text-color);
  background: var(--jq-menu-parent-active-bg);
}

.jq-header {
  margin: 14px 16px 0;
  height: 76px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.jq-user {
  font-size: 18px;
  font-weight: 600;
  color: var(--jq-card-title);
}

.jq-role {
  margin-top: 4px;
  font-size: 13px;
  color: var(--jq-card-subtitle);
}

.jq-main {
  padding: 16px;
  min-height: 0;
  overflow: auto;
}

.jq-header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.notice-badge {
  display: inline-flex;
}

.global-notice-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.global-notice-panel__summary {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  justify-content: space-between;
  color: var(--jq-card-subtitle);
  font-size: 12px;
}

.global-notice-item {
  padding: 14px 16px;
  border-radius: 16px;
  cursor: pointer;
  background: linear-gradient(135deg, rgba(217, 238, 245, 0.75), rgba(247, 244, 230, 0.8));
}

.global-notice-item__title {
  margin-top: 8px;
  font-weight: 700;
  line-height: 1.5;
  color: var(--jq-card-title);
  word-break: break-word;
}

.global-notice-item__content {
  margin-top: 8px;
  color: var(--jq-card-subtitle);
  line-height: 1.6;
  word-break: break-word;
}

.global-notice-item__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.global-notice-item__page {
  font-size: 12px;
  color: var(--jq-card-subtitle);
}

.global-notice-item__meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  color: var(--jq-card-subtitle);
  font-size: 12px;
}

:deep(.notice-dropdown-menu) {
  width: 340px;
  padding: 0;
}

.notice-dropdown-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px 8px;
  font-weight: 600;
}

.notice-dropdown-list {
  max-height: 360px;
  overflow: auto;
  padding: 0 10px 10px;
}

.notice-dropdown-item {
  padding: 10px 10px 12px;
  border-radius: 12px;
  cursor: pointer;
}

.notice-dropdown-item:hover {
  background: rgba(20, 74, 94, 0.07);
}

.notice-dropdown-item__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.notice-dropdown-item__title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-dropdown-item__meta {
  display: flex;
  justify-content: space-between;
  margin-top: 6px;
  font-size: 12px;
  color: var(--jq-card-subtitle);
}

.notice-dropdown-item__content {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--jq-card-subtitle);
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.notice-dropdown-item__dot {
  color: #d9574a;
  font-weight: 600;
}

.global-notice-item__dot {
  color: #d9574a;
  font-weight: 600;
}

.notice-dropdown-empty {
  padding: 20px 14px;
  text-align: center;
  color: var(--jq-card-subtitle);
}

:deep(.el-dropdown-menu__item.is-current) {
  color: var(--jq-menu-active-text-color);
  font-weight: 600;
}

@media (max-width: 768px) {
  .jq-header {
    height: auto;
    flex-wrap: wrap;
    gap: 12px;
    padding: 14px 16px;
  }

  .jq-header-actions {
    width: 100%;
    justify-content: flex-end;
    flex-wrap: wrap;
  }
}
</style>

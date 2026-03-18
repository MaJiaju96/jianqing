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
                    <div class="notice-dropdown-item__meta">
                      <span>{{ item.publishedAt || '刚刚发布' }}</span>
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
</template>

<script setup>
import { Bell } from '@element-plus/icons-vue';
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { logout } from '../api/auth';
import { fetchMyLatestNotices, fetchMyNoticeUnreadCount } from '../api/system';
import { NOTICE_LEVEL_OPTIONS } from '../constants/app';
import { authStore } from '../stores/auth';
import { THEMES, themeStore } from '../stores/theme.js';
import { usePermissionGroup } from '../composables/usePermissions';
import { ignoreHandledError } from '../utils/errors';
import { showSuccessMessage } from '../utils/feedback';

const route = useRoute();
const router = useRouter();

const latestNotices = ref([]);
const unreadCount = ref(0);
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
    return;
  }
  try {
    const [count, latest] = await Promise.all([fetchMyNoticeUnreadCount(), fetchMyLatestNotices(5)]);
    unreadCount.value = Number(count || 0);
    latestNotices.value = latest || [];
  } catch (error) {
    ignoreHandledError(error);
  }
}

function handleNoticeVisibleChange(visible) {
  if (visible) {
    loadNoticePanel();
  }
}

function openNotice(noticeId) {
  router.push(`/messages/mine/${noticeId}`);
}

watch(() => route.fullPath, () => {
  loadNoticePanel();
}, { immediate: true });
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

.notice-dropdown-item__dot {
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
</style>

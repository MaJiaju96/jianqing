<template>
  <el-container class="jq-shell">
    <el-aside width="248px" class="jq-aside">
      <div class="jq-logo">简擎 · Admin</div>
      <el-menu :default-active="activePath" router class="jq-menu">
        <el-menu-item index="/dashboard">仪表盘</el-menu-item>
        <el-sub-menu v-if="showSystemMenu" index="/system">
          <template #title>系统管理</template>
          <el-menu-item v-if="canViewUsers" index="/system/users">用户管理</el-menu-item>
          <el-menu-item v-if="canViewRoles" index="/system/roles">角色管理</el-menu-item>
          <el-menu-item v-if="canViewMenus" index="/system/menus">菜单权限</el-menu-item>
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
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { logout } from '../api/auth';
import { authStore } from '../stores/auth';
import { THEMES, themeStore } from '../stores/theme.js';
import { usePermissionGroup } from '../composables/usePermissions';

const route = useRoute();
const router = useRouter();

const activePath = computed(() => route.path);
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
  canViewRoles,
  canViewMenus,
  canViewOperLogs,
  canViewLoginLogs
} = usePermissionGroup({
  canViewUsers: 'system:user:list',
  canViewRoles: 'system:role:list',
  canViewMenus: 'system:menu:list',
  canViewOperLogs: 'audit:oper-log:list',
  canViewLoginLogs: 'audit:login-log:list'
});
const showSystemMenu = computed(() => canViewUsers.value || canViewRoles.value || canViewMenus.value);
const showAuditMenu = computed(() => canViewOperLogs.value || canViewLoginLogs.value);

function handleThemeChange(themeKey) {
  themeStore.setTheme(themeKey);
}

async function handleLogout() {
  try {
    await logout();
  } finally {
    authStore.logout();
    router.replace('/login');
  }
}
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
  margin: 14px 14px 0;
  height: 72px;
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

.el-main {
  padding: 16px;
}

.jq-header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

:deep(.el-dropdown-menu__item.is-current) {
  color: var(--jq-menu-active-text-color);
  font-weight: 600;
}
</style>

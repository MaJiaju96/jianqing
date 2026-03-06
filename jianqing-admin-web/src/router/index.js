import { createRouter, createWebHashHistory } from 'vue-router';
import MainLayout from '../layouts/MainLayout.vue';
import LoginView from '../views/LoginView.vue';
import DashboardView from '../views/DashboardView.vue';
import UsersView from '../views/system/UsersView.vue';
import RolesView from '../views/system/RolesView.vue';
import MenusView from '../views/system/MenusView.vue';
import OperLogsView from '../views/audit/OperLogsView.vue';
import LoginLogsView from '../views/audit/LoginLogsView.vue';
import { authStore } from '../stores/auth';
import { fetchProfile } from '../api/auth';
import { hasPerm } from '../utils/permission';

const routes = [
  { path: '/login', component: LoginView },
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', redirect: '/dashboard' },
      { path: '/dashboard', component: DashboardView },
      { path: '/system/users', component: UsersView, meta: { perm: 'system:user:list' } },
      { path: '/system/roles', component: RolesView, meta: { perm: 'system:role:list' } },
      { path: '/system/menus', component: MenusView, meta: { perm: 'system:menu:list' } },
      { path: '/audit/oper-logs', component: OperLogsView, meta: { perm: 'audit:oper-log:list' } },
      { path: '/audit/login-logs', component: LoginLogsView, meta: { perm: 'audit:login-log:list' } }
    ]
  }
];

export const router = createRouter({
  history: createWebHashHistory(),
  routes
});

router.beforeEach(async (to, _from, next) => {
  if (to.path === '/login') {
    if (authStore.isLoggedIn()) {
      next('/dashboard');
      return;
    }
    next();
    return;
  }
  if (!authStore.isLoggedIn()) {
    next('/login');
    return;
  }
  if (!authStore.profile) {
    try {
      const profile = await fetchProfile();
      authStore.setProfile(profile);
    } catch {
      authStore.logout();
      next('/login');
      return;
    }
  }
  const requiredPerm = to.meta?.perm;
  if (requiredPerm && !hasPerm(requiredPerm)) {
    next('/dashboard');
    return;
  }
  next();
});

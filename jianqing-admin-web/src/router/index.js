import { createRouter, createWebHashHistory } from 'vue-router';
import { authStore } from '../stores/auth';
import { fetchProfile } from '../api/auth';
import { hasPerm } from '../utils/permission';

const MainLayout = () => import('../layouts/MainLayout.vue');
const LoginView = () => import('../views/LoginView.vue');
const DashboardView = () => import('../views/DashboardView.vue');
const DeptsView = () => import('../views/system/DeptsView.vue');
const ConfigsView = () => import('../views/system/ConfigsView.vue');
const DictsView = () => import('../views/system/DictsView.vue');
const GeneratorView = () => import('../views/system/GeneratorView.vue');
const UsersView = () => import('../views/system/UsersView.vue');
const RolesView = () => import('../views/system/RolesView.vue');
const MenusView = () => import('../views/system/MenusView.vue');
const OperLogsView = () => import('../views/audit/OperLogsView.vue');
const LoginLogsView = () => import('../views/audit/LoginLogsView.vue');

const routes = [
  { path: '/login', component: LoginView },
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', redirect: '/dashboard' },
      { path: '/dashboard', component: DashboardView },
      { path: '/system/depts', component: DeptsView, meta: { perm: 'system:dept:list' } },
      { path: '/system/config', component: ConfigsView, meta: { perm: 'system:config:list' } },
      { path: '/system/dicts', component: DictsView, meta: { perm: 'system:dict:list' } },
      { path: '/system/generator', component: GeneratorView, meta: { perm: 'system:generator:list' } },
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

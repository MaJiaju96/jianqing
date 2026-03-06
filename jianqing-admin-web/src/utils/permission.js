import { authStore } from '../stores/auth';

export function hasPerm(permission) {
  const profile = authStore.profile;
  if (!profile) {
    return false;
  }
  if ((profile.roles || []).includes('super_admin')) {
    return true;
  }
  return (profile.permissions || []).includes(permission);
}

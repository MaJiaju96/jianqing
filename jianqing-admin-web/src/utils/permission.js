import { SUPER_ADMIN_ROLE } from '../constants/app';
import { authStore } from '../stores/auth';

export function hasPerm(permission) {
  const profile = authStore.profile;
  if (!profile) {
    return false;
  }
  if ((profile.roles || []).includes(SUPER_ADMIN_ROLE)) {
    return true;
  }
  return (profile.permissions || []).includes(permission);
}

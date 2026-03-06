import { computed } from 'vue';
import { hasPerm } from '../utils/permission';

export function usePermission(permission) {
  return computed(() => hasPerm(permission));
}

export function usePermissionGroup(permissionMap) {
  return Object.entries(permissionMap).reduce((result, [key, permission]) => {
    result[key] = computed(() => hasPerm(permission));
    return result;
  }, {});
}

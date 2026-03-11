import { computed, ref } from 'vue';
import { fetchMenuTree, fetchRoles, fetchUsers } from '../api/system';
import { ignoreHandledError } from '../utils/errors';
import { hasPerm } from '../utils/permission';

export function useOverviewCounts() {
  const usersCount = ref(null);
  const rolesCount = ref(null);
  const menuCount = ref(null);

  const usersCountDisplay = computed(() => (usersCount.value == null ? '--' : usersCount.value));
  const rolesCountDisplay = computed(() => (rolesCount.value == null ? '--' : rolesCount.value));
  const menuCountDisplay = computed(() => (menuCount.value == null ? '--' : menuCount.value));

  async function loadCounts() {
    try {
      const requests = [];
      if (hasPerm('system:user:list')) {
        requests.push(fetchUsers().then((users) => {
          usersCount.value = users.length;
        }));
      }
      if (hasPerm('system:role:list')) {
        requests.push(fetchRoles().then((roles) => {
          rolesCount.value = roles.length;
        }));
      }
      if (hasPerm('system:menu:list')) {
        requests.push(fetchMenuTree().then((menus) => {
          menuCount.value = countMenus(menus);
        }));
      }
      await Promise.all(requests);
    } catch (error) {
      ignoreHandledError(error);
    }
  }

  return {
    usersCountDisplay,
    rolesCountDisplay,
    menuCountDisplay,
    loadCounts
  };
}

function countMenus(tree) {
  return tree.reduce((total, item) => total + 1 + countMenus(item.children || []), 0);
}

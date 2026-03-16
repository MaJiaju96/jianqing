import { http } from './http';

export function fetchUsers() {
  return http.get('/system/users');
}

export function createUser(payload) {
  return http.post('/system/users', payload);
}

export function updateUser(id, payload) {
  return http.post(`/system/users/${id}/update`, payload);
}

export function deleteUser(id) {
  return http.post(`/system/users/${id}/delete`);
}

export function fetchUserRoleIds(userId) {
  return http.get(`/system/users/${userId}/role-ids`);
}

export function assignUserRoles(userId, ids) {
  return http.post(`/system/users/${userId}/roles/assign`, { ids });
}

export function fetchRoles() {
  return http.get('/system/roles');
}

export function fetchDictTypes() {
  return http.get('/system/dict-types');
}

export function createDictType(payload) {
  return http.post('/system/dict-types', payload);
}

export function updateDictType(id, payload) {
  return http.post(`/system/dict-types/${id}/update`, payload);
}

export function deleteDictType(id) {
  return http.post(`/system/dict-types/${id}/delete`);
}

export function fetchDictData(dictType) {
  return http.get('/system/dict-data', {
    params: { dictType }
  });
}

export function fetchDictOptions(dictType) {
  return http.get(`/system/dict-options/${dictType}`);
}

export function fetchConfigs() {
  return http.get('/system/configs');
}

export function createConfig(payload) {
  return http.post('/system/configs', payload);
}

export function updateConfig(id, payload) {
  return http.post(`/system/configs/${id}/update`, payload);
}

export function deleteConfig(id) {
  return http.post(`/system/configs/${id}/delete`);
}

export function fetchConfigHistory(id) {
  return http.get(`/system/configs/${id}/history`);
}

export function fetchDeletedConfigHistory() {
  return http.get('/system/configs/deleted/history');
}

export function fetchDeletedConfigRestorePreview(historyId) {
  return http.get(`/system/configs/deleted/history/${historyId}/preview`);
}

export function rollbackConfig(id, historyId) {
  return http.post(`/system/configs/${id}/history/${historyId}/rollback`);
}

export function restoreDeletedConfig(historyId) {
  return http.post(`/system/configs/history/${historyId}/restore`);
}

export function fetchConfigDiff(id, historyId, compareHistoryId) {
  return http.get(`/system/configs/${id}/history/${historyId}/diff`, {
    params: compareHistoryId ? { compareHistoryId } : undefined
  });
}

export function createDictData(payload) {
  return http.post('/system/dict-data', payload);
}

export function updateDictData(id, payload) {
  return http.post(`/system/dict-data/${id}/update`, payload);
}

export function deleteDictData(id) {
  return http.post(`/system/dict-data/${id}/delete`);
}

export function fetchDepts() {
  return http.get('/system/depts');
}

export function createDept(payload) {
  return http.post('/system/depts', payload);
}

export function updateDept(id, payload) {
  return http.post(`/system/depts/${id}/update`, payload);
}

export function deleteDept(id) {
  return http.post(`/system/depts/${id}/delete`);
}

export function createRole(payload) {
  return http.post('/system/roles', payload);
}

export function updateRole(id, payload) {
  return http.post(`/system/roles/${id}/update`, payload);
}

export function deleteRole(id) {
  return http.post(`/system/roles/${id}/delete`);
}

export function fetchRoleMenuIds(roleId) {
  return http.get(`/system/roles/${roleId}/menu-ids`);
}

export function assignRoleMenus(roleId, ids) {
  return http.post(`/system/roles/${roleId}/menus/assign`, { ids });
}

export function fetchMenus() {
  return http.get('/system/menus');
}

export function fetchMenuTree() {
  return http.get('/system/menus/tree');
}

export function createMenu(payload) {
  return http.post('/system/menus', payload);
}

export function updateMenu(id, payload) {
  return http.post(`/system/menus/${id}/update`, payload);
}

export function deleteMenu(id) {
  return http.post(`/system/menus/${id}/delete`);
}

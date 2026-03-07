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

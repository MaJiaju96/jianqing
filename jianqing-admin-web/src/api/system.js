import { http } from './http';
import { authStore } from '../stores/auth';

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

export function fetchNotices() {
  return http.get('/system/notices');
}

export function fetchNoticeTrash(category = 'all') {
  return http.get('/system/notices/trash', {
    params: { category }
  });
}

export function fetchNoticeDetail(id) {
  return http.get(`/system/notices/${id}`);
}

export function createNotice(payload) {
  return http.post('/system/notices', payload);
}

export function updateNotice(id, payload) {
  return http.post(`/system/notices/${id}/update`, payload);
}

export function publishNotice(id) {
  return http.post(`/system/notices/${id}/publish`);
}

export function cancelNotice(id) {
  return http.post(`/system/notices/${id}/cancel`);
}

export function deleteNotice(id) {
  return http.post(`/system/notices/${id}/delete`);
}

export function restoreNotice(id) {
  return http.post(`/system/notices/${id}/restore`);
}

export function purgeNotice(id) {
  return http.post(`/system/notices/${id}/purge`);
}

export function fetchMyNotices() {
  return http.get('/system/my-notices');
}

export function fetchMyNoticeDetail(id) {
  return http.get(`/system/my-notices/${id}`);
}

export function fetchMyNoticeUnreadCount() {
  return http.get('/system/my-notices/unread-count');
}

export function fetchMyNoticeRealtime() {
  return http.get('/system/my-notices/realtime');
}

export function fetchMyLatestNotices(limit = 5) {
  return http.get('/system/my-notices/latest', {
    params: { limit }
  });
}

export function fetchMyPopupCandidates(limit = 3) {
  return http.get('/system/my-notices/popup-candidate', {
    params: { limit }
  });
}

export function markMyNoticeRead(id) {
  return http.post(`/system/my-notices/${id}/read`);
}

export function markAllMyNoticesRead() {
  return http.post('/system/my-notices/read-all');
}

export async function subscribeMyNoticeStream(onMessage, onOpen, onError, signal) {
  const response = await fetch('/api/system/my-notices/stream', {
    method: 'GET',
    headers: {
      Accept: 'text/event-stream',
      Authorization: authStore.token ? `Bearer ${authStore.token}` : ''
    },
    cache: 'no-store',
    signal
  });
  if (!response.ok || !response.body) {
    throw new Error(`通知流连接失败: ${response.status}`);
  }
  if (onOpen) {
    onOpen();
  }
  const reader = response.body.getReader();
  const decoder = new TextDecoder('utf-8');
  let buffer = '';

  while (true) {
    const { value, done } = await reader.read();
    if (done) {
      break;
    }
    buffer += decoder.decode(value, { stream: true });
    const chunks = buffer.split('\n\n');
    buffer = chunks.pop() || '';
    chunks.forEach((chunk) => {
      try {
        const event = parseSseChunk(chunk);
        if (event?.event === 'notice-sync' && event.data && onMessage) {
          onMessage(JSON.parse(event.data));
        }
      } catch (error) {
        if (onError) {
          onError(error);
        }
      }
    });
  }
}

function parseSseChunk(chunk) {
  if (!chunk) {
    return null;
  }
  const lines = chunk.split('\n');
  const event = { event: 'message', data: '' };
  lines.forEach((line) => {
    if (line.startsWith('event:')) {
      event.event = line.slice(6).trim();
      return;
    }
    if (line.startsWith('data:')) {
      event.data += line.slice(5).trim();
    }
  });
  return event;
}

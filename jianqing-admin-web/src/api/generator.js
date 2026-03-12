import axios from 'axios';
import { http } from './http';
import { authStore } from '../stores/auth';

export function fetchGeneratorTables() {
  return http.get('/dev/gen/tables');
}

export function fetchGeneratorColumns(tableName) {
  return http.get(`/dev/gen/tables/${tableName}/columns`);
}

export function previewGenerator(payload) {
  return http.post('/dev/gen/preview', payload);
}

export function writeGenerator(payload, options = {}) {
  const {
    projectRoot = '',
    overwrite = false
  } = options;
  return http.post('/dev/gen/write', payload, { params: { projectRoot, overwrite } });
}

export function deleteGeneratorByMarker(markerId, options = {}) {
  const { projectRoot = '' } = options;
  return http.post('/dev/gen/write/delete-by-marker', null, { params: { markerId, projectRoot } });
}

export function fetchGeneratorWriteRecords(options = {}) {
  const {
    limit = 20,
    tableName = '',
    startAt = '',
    endAt = ''
  } = options;
  return http.get('/dev/gen/write/records', {
    params: { limit, tableName, startAt, endAt }
  });
}

export function fetchGeneratorWriteConflicts(payload, options = {}) {
  const { projectRoot = '' } = options;
  return http.post('/dev/gen/write/conflicts', payload, { params: { projectRoot } });
}

export async function downloadGeneratorZip(payload) {
  const response = await axios.post('/api/dev/gen/download', payload, {
    responseType: 'blob',
    headers: authStore.token
      ? {
          Authorization: `Bearer ${authStore.token}`
        }
      : undefined
  });
  return {
    blob: response.data,
    fileName: parseFileName(response.headers['content-disposition'])
  };
}

function parseFileName(contentDisposition) {
  const matched = contentDisposition?.match(/filename="?([^";]+)"?/);
  return matched?.[1] || 'generator-preview.zip';
}

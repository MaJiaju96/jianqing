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

export function writeGenerator(payload, projectRoot = '') {
  return http.post('/dev/gen/write', payload, { params: { projectRoot } });
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

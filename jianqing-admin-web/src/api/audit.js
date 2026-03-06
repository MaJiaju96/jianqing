import { http } from './http';

function buildQuery(params) {
  const query = new URLSearchParams();
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== '' && value !== null && value !== undefined) {
      query.append(key, value);
    }
  });
  return query.toString();
}

export function fetchOperLogs(params = {}) {
  return http.get(`/audit/oper-logs?${buildQuery(params)}`);
}

export function fetchLoginLogs(params = {}) {
  return http.get(`/audit/login-logs?${buildQuery(params)}`);
}

import { http } from './http';

export function login(username, password) {
  return http.post('/auth/login', { username, password });
}

export function fetchProfile() {
  return http.get('/auth/me');
}

export function logout() {
  return http.post('/auth/logout');
}

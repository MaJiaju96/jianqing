import { reactive } from 'vue';

const TOKEN_KEY = 'jq_access_token';

const state = reactive({
  token: localStorage.getItem(TOKEN_KEY) ?? '',
  profile: null
});

export const authStore = {
  get token() {
    return state.token;
  },
  get profile() {
    return state.profile;
  },
  isLoggedIn() {
    return Boolean(state.token);
  },
  setToken(token) {
    state.token = token;
    localStorage.setItem(TOKEN_KEY, token);
  },
  setProfile(profile) {
    state.profile = profile;
  },
  logout() {
    state.token = '';
    state.profile = null;
    localStorage.removeItem(TOKEN_KEY);
  }
};

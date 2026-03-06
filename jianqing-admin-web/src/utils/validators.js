const MOBILE_PATTERN = /^1\d{10}$/;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const ROLE_CODE_PATTERN = /^[a-zA-Z0-9:_-]{2,64}$/;
const PERMISSION_CODE_PATTERN = /^[a-zA-Z0-9:_-]{2,128}$/;

export function isValidMobile(value) {
  return MOBILE_PATTERN.test(value);
}

export function isValidEmail(value) {
  return EMAIL_PATTERN.test(value);
}

export function isValidRoleCode(value) {
  return ROLE_CODE_PATTERN.test(value);
}

export function isValidPermissionCode(value) {
  return PERMISSION_CODE_PATTERN.test(value);
}

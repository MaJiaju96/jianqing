export const API_SUCCESS_CODE = 0;

export const PAGE_SIZE_OPTIONS = [10, 20, 50];
export const DEFAULT_LIST_PAGE_SIZE = 10;
export const DEFAULT_AUDIT_PAGE_SIZE = 20;

export const STATUS_FILTER_ALL = 'all';
export const STATUS_ENABLED = 1;
export const STATUS_DISABLED = 0;

export const COMMON_STATUS_OPTIONS = [
  { label: '启用', value: '1', colorType: 'success' },
  { label: '禁用', value: '0', colorType: 'danger' }
];

export const DEPT_STATUS_OPTIONS = [
  { label: '启用', value: '1', colorType: 'success' },
  { label: '禁用', value: '0', colorType: 'info' }
];

export const EMPTY_FILTER_VALUE = '';

export const MENU_TYPE_DIRECTORY = 1;
export const MENU_TYPE_PAGE = 2;
export const MENU_TYPE_BUTTON = 3;

export const SUPER_ADMIN_ROLE = 'super_admin';
export const ROOT_PARENT_ID = 0;

export const DATA_SCOPE_ALL = 1;
export const DATA_SCOPE_DEPT_AND_CHILD = 2;
export const DATA_SCOPE_DEPT = 3;
export const DATA_SCOPE_SELF = 4;
export const DATA_SCOPE_CUSTOM = 5;

export const DATA_SCOPE_OPTIONS = [
  { label: '全部数据', value: DATA_SCOPE_ALL },
  { label: '本部门及以下数据', value: DATA_SCOPE_DEPT_AND_CHILD },
  { label: '本部门数据', value: DATA_SCOPE_DEPT },
  { label: '仅本人数据', value: DATA_SCOPE_SELF },
  { label: '自定义部门数据', value: DATA_SCOPE_CUSTOM }
];

export const NOTICE_LEVEL_OPTIONS = [
  { label: '普通', value: 'NORMAL', tagType: 'info' },
  { label: '重要', value: 'IMPORTANT', tagType: 'warning' },
  { label: '紧急', value: 'URGENT', tagType: 'danger' }
];

export const NOTICE_PUBLISH_MODE_OPTIONS = [
  { label: '立即发布', value: 'IMMEDIATE' },
  { label: '定时发布', value: 'SCHEDULED' }
];

export const NOTICE_TARGET_TYPE_OPTIONS = [
  { label: '全员', value: 'ALL' },
  { label: '角色', value: 'ROLE' },
  { label: '部门', value: 'DEPT' },
  { label: '指定用户', value: 'USER' }
];

export const NOTICE_STATUS_OPTIONS = [
  { label: '草稿', value: 'DRAFT', tagType: 'info' },
  { label: '待发布', value: 'PENDING', tagType: 'warning' },
  { label: '已发布', value: 'PUBLISHED', tagType: 'success' },
  { label: '已取消', value: 'CANCELLED', tagType: 'info' }
];

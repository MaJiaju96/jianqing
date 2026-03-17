export const API_SUCCESS_CODE = 0;

export const PAGE_SIZE_OPTIONS = [10, 20, 50];
export const DEFAULT_LIST_PAGE_SIZE = 10;
export const DEFAULT_AUDIT_PAGE_SIZE = 20;

export const STATUS_FILTER_ALL = 'all';
export const STATUS_ENABLED = 1;
export const STATUS_DISABLED = 0;

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

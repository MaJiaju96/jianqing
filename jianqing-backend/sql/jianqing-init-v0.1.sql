CREATE DATABASE IF NOT EXISTS jianqing DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE jianqing;

CREATE TABLE IF NOT EXISTS jq_sys_user (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  username        VARCHAR(64)  NOT NULL,
  password_hash   VARCHAR(255) NOT NULL,
  nickname        VARCHAR(64)  NOT NULL DEFAULT '',
  real_name       VARCHAR(64)  NOT NULL DEFAULT '',
  mobile          VARCHAR(20)  NOT NULL DEFAULT '',
  email           VARCHAR(128) NOT NULL DEFAULT '',
  dept_id         BIGINT       NOT NULL DEFAULT 0,
  status          TINYINT      NOT NULL DEFAULT 1,
  is_deleted      TINYINT      NOT NULL DEFAULT 0,
  last_login_ip   VARCHAR(64)  NOT NULL DEFAULT '',
  last_login_time DATETIME     NULL,
  remark          VARCHAR(255) NOT NULL DEFAULT '',
  created_by      BIGINT       NOT NULL DEFAULT 0,
  updated_by      BIGINT       NOT NULL DEFAULT 0,
  created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_jq_sys_user_username (username),
  KEY idx_jq_sys_user_dept_id (dept_id),
  KEY idx_jq_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS jq_sys_role (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_name       VARCHAR(64)  NOT NULL,
  role_code       VARCHAR(64)  NOT NULL,
  data_scope      TINYINT      NOT NULL DEFAULT 1 COMMENT '1全部 2本部门及子部门 3本部门 4本人 5自定义部门',
  status          TINYINT      NOT NULL DEFAULT 1,
  is_deleted      TINYINT      NOT NULL DEFAULT 0,
  remark          VARCHAR(255) NOT NULL DEFAULT '',
  created_by      BIGINT       NOT NULL DEFAULT 0,
  updated_by      BIGINT       NOT NULL DEFAULT 0,
  created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_jq_sys_role_role_code (role_code),
  KEY idx_jq_sys_role_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS jq_sys_menu (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id       BIGINT       NOT NULL DEFAULT 0,
  menu_type       TINYINT      NOT NULL COMMENT '1目录 2菜单 3按钮',
  menu_name       VARCHAR(64)  NOT NULL,
  route_path      VARCHAR(128) NOT NULL DEFAULT '',
  component       VARCHAR(255) NOT NULL DEFAULT '',
  perms           VARCHAR(128) NOT NULL DEFAULT '',
  icon            VARCHAR(64)  NOT NULL DEFAULT '',
  sort_no         INT          NOT NULL DEFAULT 0,
  visible         TINYINT      NOT NULL DEFAULT 1,
  status          TINYINT      NOT NULL DEFAULT 1,
  is_deleted      TINYINT      NOT NULL DEFAULT 0,
  created_by      BIGINT       NOT NULL DEFAULT 0,
  updated_by      BIGINT       NOT NULL DEFAULT 0,
  created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_jq_sys_menu_parent_id (parent_id),
  KEY idx_jq_sys_menu_menu_type (menu_type),
  KEY idx_jq_sys_menu_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS jq_sys_dept (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id       BIGINT       NOT NULL DEFAULT 0,
  dept_name       VARCHAR(64)  NOT NULL,
  leader_user_id  BIGINT       NOT NULL DEFAULT 0,
  phone           VARCHAR(20)  NOT NULL DEFAULT '',
  email           VARCHAR(128) NOT NULL DEFAULT '',
  sort_no         INT          NOT NULL DEFAULT 0,
  status          TINYINT      NOT NULL DEFAULT 1,
  is_deleted      TINYINT      NOT NULL DEFAULT 0,
  created_by      BIGINT       NOT NULL DEFAULT 0,
  updated_by      BIGINT       NOT NULL DEFAULT 0,
  created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_jq_sys_dept_parent_id (parent_id),
  KEY idx_jq_sys_dept_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS jq_sys_user_role (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id         BIGINT       NOT NULL,
  role_id         BIGINT       NOT NULL,
  created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_jq_sys_user_role (user_id, role_id),
  KEY idx_jq_sys_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS jq_sys_role_menu (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id         BIGINT       NOT NULL,
  menu_id         BIGINT       NOT NULL,
  created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_jq_sys_role_menu (role_id, menu_id),
  KEY idx_jq_sys_role_menu_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS jq_sys_dict_type (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  dict_name       VARCHAR(64)  NOT NULL,
  dict_type       VARCHAR(64)  NOT NULL,
  status          TINYINT      NOT NULL DEFAULT 1,
  remark          VARCHAR(255) NOT NULL DEFAULT '',
  created_by      BIGINT       NOT NULL DEFAULT 0,
  updated_by      BIGINT       NOT NULL DEFAULT 0,
  created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_jq_sys_dict_type_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS jq_sys_dict_data (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  dict_type       VARCHAR(64)  NOT NULL,
  label           VARCHAR(64)  NOT NULL,
  value           VARCHAR(64)  NOT NULL,
  color_type      VARCHAR(32)  NOT NULL DEFAULT '',
  css_class       VARCHAR(64)  NOT NULL DEFAULT '',
  sort_no         INT          NOT NULL DEFAULT 0,
  status          TINYINT      NOT NULL DEFAULT 1,
  remark          VARCHAR(255) NOT NULL DEFAULT '',
  created_by      BIGINT       NOT NULL DEFAULT 0,
  updated_by      BIGINT       NOT NULL DEFAULT 0,
  created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_jq_sys_dict_data_type_value (dict_type, value),
  KEY idx_jq_sys_dict_data_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS jq_sys_config (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_key      VARCHAR(128)  NOT NULL,
  config_value    VARCHAR(1024) NOT NULL DEFAULT '',
  config_name     VARCHAR(128)  NOT NULL,
  config_group    VARCHAR(64)   NOT NULL DEFAULT 'DEFAULT_GROUP',
  value_type      VARCHAR(32)   NOT NULL DEFAULT 'string',
  is_builtin      TINYINT       NOT NULL DEFAULT 0,
  remark          VARCHAR(255)  NOT NULL DEFAULT '',
  created_by      BIGINT        NOT NULL DEFAULT 0,
  updated_by      BIGINT        NOT NULL DEFAULT 0,
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_jq_sys_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS jq_sys_config_history (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_id       BIGINT        NOT NULL DEFAULT 0,
  config_key      VARCHAR(128)  NOT NULL,
  config_name     VARCHAR(128)  NOT NULL,
  config_group    VARCHAR(64)   NOT NULL DEFAULT 'DEFAULT_GROUP',
  config_value    VARCHAR(1024) NOT NULL DEFAULT '',
  value_type      VARCHAR(32)   NOT NULL DEFAULT 'string',
  is_builtin      TINYINT       NOT NULL DEFAULT 0,
  change_type     VARCHAR(32)   NOT NULL DEFAULT 'UPDATE',
  change_note     VARCHAR(255)  NOT NULL DEFAULT '',
  created_by      BIGINT        NOT NULL DEFAULT 0,
  updated_by      BIGINT        NOT NULL DEFAULT 0,
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_jq_sys_config_history_config_id (config_id),
  KEY idx_jq_sys_config_history_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO jq_sys_config
(config_key, config_value, config_name, config_group, value_type, is_builtin, remark, created_by, updated_by)
VALUES
('sys.theme.default', 'midnight-blue', '默认主题', 'UI', 'string', 1, '前端默认主题', 1, 1),
('sys.login.captcha.enabled', 'false', '登录验证码开关', 'AUTH', 'boolean', 0, '预留登录验证码控制', 1, 1)
ON DUPLICATE KEY UPDATE
config_value = VALUES(config_value),
config_name = VALUES(config_name),
config_group = VALUES(config_group),
value_type = VALUES(value_type),
is_builtin = VALUES(is_builtin),
remark = VALUES(remark),
updated_by = VALUES(updated_by);

INSERT INTO jq_sys_dict_type
(dict_name, dict_type, status, remark, created_by, updated_by)
VALUES
('通用状态', 'sys_common_status', 1, '用户/角色/菜单通用启禁用状态', 1, 1),
('菜单可见性', 'sys_menu_visible', 1, '菜单显示/隐藏状态', 1, 1),
('部门状态', 'sys_dept_status', 1, '部门启用/停用状态', 1, 1),
('审计执行状态', 'audit_exec_status', 1, '审计日志成功/失败状态', 1, 1),
('登录方式', 'audit_login_type', 1, '登录日志登录方式', 1, 1),
('参数值类型', 'sys_config_value_type', 1, '系统参数值类型选项', 1, 1),
('字典颜色类型', 'sys_dict_color_type', 1, '字典数据颜色类型选项', 1, 1),
('参数来源', 'sys_config_source', 1, '系统参数来源选项', 1, 1)
ON DUPLICATE KEY UPDATE
dict_name = VALUES(dict_name),
status = VALUES(status),
remark = VALUES(remark),
updated_by = VALUES(updated_by);

INSERT INTO jq_sys_dict_data
(dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
VALUES
('sys_common_status', '启用', '1', 'success', '', 1, 1, '启用', 1, 1),
('sys_common_status', '禁用', '0', 'danger', '', 2, 1, '禁用', 1, 1),
('sys_menu_visible', '显示', '1', 'info', '', 1, 1, '菜单显示', 1, 1),
('sys_menu_visible', '隐藏', '0', 'warning', '', 2, 1, '菜单隐藏', 1, 1),
('sys_dept_status', '启用', '1', 'success', '', 1, 1, '部门启用', 1, 1),
('sys_dept_status', '停用', '0', 'info', '', 2, 1, '部门停用', 1, 1),
('audit_exec_status', '成功', '1', 'success', '', 1, 1, '审计成功', 1, 1),
('audit_exec_status', '失败', '0', 'danger', '', 2, 1, '审计失败', 1, 1),
('audit_login_type', '密码', 'password', 'info', '', 1, 1, '密码登录', 1, 1),
('sys_config_value_type', '字符串', 'string', 'info', '', 1, 1, '字符串参数值', 1, 1),
('sys_config_value_type', '数字', 'number', 'warning', '', 2, 1, '数字参数值', 1, 1),
('sys_config_value_type', '布尔值', 'boolean', 'success', '', 3, 1, '布尔参数值', 1, 1),
('sys_config_value_type', 'JSON', 'json', 'primary', '', 4, 1, 'JSON 参数值', 1, 1),
('sys_dict_color_type', '默认', '', '', '', 1, 1, '默认颜色类型', 1, 1),
('sys_dict_color_type', '主色', 'primary', 'primary', '', 2, 1, '主色标签', 1, 1),
('sys_dict_color_type', '成功', 'success', 'success', '', 3, 1, '成功标签', 1, 1),
('sys_dict_color_type', '信息', 'info', 'info', '', 4, 1, '信息标签', 1, 1),
('sys_dict_color_type', '警告', 'warning', 'warning', '', 5, 1, '警告标签', 1, 1),
('sys_dict_color_type', '危险', 'danger', 'danger', '', 6, 1, '危险标签', 1, 1),
('sys_config_source', '自定义', '0', 'info', '', 1, 1, '自定义参数', 1, 1),
('sys_config_source', '内置', '1', 'warning', '', 2, 1, '内置参数', 1, 1)
ON DUPLICATE KEY UPDATE
label = VALUES(label),
color_type = VALUES(color_type),
css_class = VALUES(css_class),
sort_no = VALUES(sort_no),
status = VALUES(status),
remark = VALUES(remark),
updated_by = VALUES(updated_by);

CREATE TABLE IF NOT EXISTS jq_sys_oper_log (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  trace_id        VARCHAR(64)   NOT NULL DEFAULT '',
  user_id         BIGINT        NOT NULL DEFAULT 0,
  username        VARCHAR(64)   NOT NULL DEFAULT '',
  module_name     VARCHAR(64)   NOT NULL DEFAULT '',
  biz_type        VARCHAR(32)   NOT NULL DEFAULT '',
  method          VARCHAR(255)  NOT NULL DEFAULT '',
  request_uri     VARCHAR(255)  NOT NULL DEFAULT '',
  request_ip      VARCHAR(64)   NOT NULL DEFAULT '',
  request_param   LONGTEXT      NULL,
  response_data   LONGTEXT      NULL,
  status          TINYINT       NOT NULL DEFAULT 1,
  error_msg       VARCHAR(1000) NOT NULL DEFAULT '',
  cost_ms         INT           NOT NULL DEFAULT 0,
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_jq_sys_oper_log_user_id (user_id),
  KEY idx_jq_sys_oper_log_created_at (created_at),
  KEY idx_jq_sys_oper_log_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS jq_sys_login_log (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id         BIGINT       NOT NULL DEFAULT 0,
  username        VARCHAR(64)  NOT NULL DEFAULT '',
  login_type      VARCHAR(32)  NOT NULL DEFAULT 'password',
  login_ip        VARCHAR(64)  NOT NULL DEFAULT '',
  login_location  VARCHAR(128) NOT NULL DEFAULT '',
  user_agent      VARCHAR(512) NOT NULL DEFAULT '',
  os              VARCHAR(64)  NOT NULL DEFAULT '',
  browser         VARCHAR(64)  NOT NULL DEFAULT '',
  status          TINYINT      NOT NULL DEFAULT 1,
  msg             VARCHAR(255) NOT NULL DEFAULT '',
  created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_jq_sys_login_log_user_id (user_id),
  KEY idx_jq_sys_login_log_created_at (created_at),
  KEY idx_jq_sys_login_log_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO jq_sys_dept
(parent_id, dept_name, leader_user_id, sort_no, status, is_deleted, created_by, updated_by)
VALUES
(0, '简擎总部', 0, 1, 1, 0, 1, 1);

INSERT INTO jq_sys_role
(role_name, role_code, data_scope, status, is_deleted, remark, created_by, updated_by)
VALUES
('超级管理员', 'super_admin', 1, 1, 0, '拥有全部权限', 1, 1);

INSERT INTO jq_sys_user
(username, password_hash, nickname, real_name, mobile, email, dept_id, status, is_deleted, created_by, updated_by)
VALUES
('admin', '$2a$10$REPLACE_WITH_BCRYPT_HASH', '管理员', '系统管理员', '', 'admin@jianqing.dev', 1, 1, 0, 1, 1);

INSERT INTO jq_sys_user_role (user_id, role_id)
VALUES (1, 1);

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
VALUES
(0, 1, '系统管理', '/system', 'Layout', '', 'Setting', 10, 1, 1, 0, 1, 1),

(1, 2, '用户管理', 'user', 'system/user/index', 'system:user:list', 'User', 1, 1, 1, 0, 1, 1),
(1, 2, '角色管理', 'role', 'system/role/index', 'system:role:list', 'Avatar', 2, 1, 1, 0, 1, 1),
(1, 2, '菜单管理', 'menu', 'system/menu/index', 'system:menu:list', 'Menu', 3, 1, 1, 0, 1, 1),
(1, 2, '部门管理', 'dept', 'system/dept/index', 'system:dept:list', 'OfficeBuilding', 4, 1, 1, 0, 1, 1),
(1, 2, '字典管理', 'dict', 'system/dict/index', 'system:dict:list', 'CollectionTag', 5, 1, 1, 0, 1, 1),
(1, 2, '参数设置', 'config', 'system/config/index', 'system:config:list', 'Tools', 6, 1, 1, 0, 1, 1),

(2, 3, '用户查询', '', '', 'system:user:query', '', 1, 1, 1, 0, 1, 1),
(2, 3, '用户新增', '', '', 'system:user:add', '', 2, 1, 1, 0, 1, 1),
(2, 3, '用户修改', '', '', 'system:user:edit', '', 3, 1, 1, 0, 1, 1),
(2, 3, '用户删除', '', '', 'system:user:remove', '', 4, 1, 1, 0, 1, 1),

(3, 3, '角色查询', '', '', 'system:role:query', '', 1, 1, 1, 0, 1, 1),
(3, 3, '角色新增', '', '', 'system:role:add', '', 2, 1, 1, 0, 1, 1),
(3, 3, '角色修改', '', '', 'system:role:edit', '', 3, 1, 1, 0, 1, 1),
(3, 3, '角色删除', '', '', 'system:role:remove', '', 4, 1, 1, 0, 1, 1),

(6, 3, '字典查询', '', '', 'system:dict:query', '', 1, 1, 1, 0, 1, 1),
(6, 3, '字典新增', '', '', 'system:dict:add', '', 2, 1, 1, 0, 1, 1),
(6, 3, '字典修改', '', '', 'system:dict:edit', '', 3, 1, 1, 0, 1, 1),
(6, 3, '字典删除', '', '', 'system:dict:remove', '', 4, 1, 1, 0, 1, 1),
(6, 3, '字典数据新增', '', '', 'system:dict-data:add', '', 5, 1, 1, 0, 1, 1),
(6, 3, '字典数据修改', '', '', 'system:dict-data:edit', '', 6, 1, 1, 0, 1, 1),
(6, 3, '字典数据删除', '', '', 'system:dict-data:remove', '', 7, 1, 1, 0, 1, 1),

(7, 3, '参数查询', '', '', 'system:config:query', '', 1, 1, 1, 0, 1, 1),
(7, 3, '参数新增', '', '', 'system:config:add', '', 2, 1, 1, 0, 1, 1),
(7, 3, '参数修改', '', '', 'system:config:edit', '', 3, 1, 1, 0, 1, 1),
(7, 3, '参数删除', '', '', 'system:config:remove', '', 4, 1, 1, 0, 1, 1),

(0, 1, '审计日志', '/audit', 'Layout', '', 'Document', 20, 1, 1, 0, 1, 1),
(16, 2, '操作日志', 'oper-logs', 'audit/oper/index', 'audit:oper-log:list', 'List', 1, 1, 1, 0, 1, 1),
(16, 2, '登录日志', 'login-logs', 'audit/login/index', 'audit:login-log:list', 'Tickets', 2, 1, 1, 0, 1, 1),
(17, 3, '操作日志查询', '', '', 'audit:oper-log:query', '', 1, 1, 1, 0, 1, 1),
(18, 3, '登录日志查询', '', '', 'audit:login-log:query', '', 1, 1, 1, 0, 1, 1),

(1, 2, '代码生成', 'generator', 'system/generator/index', 'system:generator:list', 'MagicStick', 7, 1, 1, 0, 1, 1),
(19, 3, '代码生成查询', '', '', 'system:generator:query', '', 1, 1, 1, 0, 1, 1);

INSERT INTO jq_sys_role_menu (role_id, menu_id)
SELECT 1, id FROM jq_sys_menu;

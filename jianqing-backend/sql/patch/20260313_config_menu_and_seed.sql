ALTER TABLE jq_sys_config
ADD COLUMN IF NOT EXISTS config_group VARCHAR(64) NOT NULL DEFAULT 'DEFAULT_GROUP' AFTER config_name;

CREATE TABLE IF NOT EXISTS jq_sys_config_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_id BIGINT NOT NULL DEFAULT 0,
  config_key VARCHAR(128) NOT NULL,
  config_name VARCHAR(128) NOT NULL,
  config_group VARCHAR(64) NOT NULL DEFAULT 'DEFAULT_GROUP',
  config_value VARCHAR(1024) NOT NULL DEFAULT '',
  value_type VARCHAR(32) NOT NULL DEFAULT 'string',
  is_builtin TINYINT NOT NULL DEFAULT 0,
  change_type VARCHAR(32) NOT NULL DEFAULT 'UPDATE',
  change_note VARCHAR(255) NOT NULL DEFAULT '',
  created_by BIGINT NOT NULL DEFAULT 0,
  updated_by BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_jq_sys_config_history_config_id (config_id),
  KEY idx_jq_sys_config_history_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO jq_sys_config
(config_key, config_value, config_name, config_group, value_type, is_builtin, remark, created_by, updated_by)
SELECT 'sys.theme.default', 'midnight-blue', '默认主题', 'UI', 'string', 1, '前端默认主题', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_config WHERE config_key = 'sys.theme.default'
);

INSERT INTO jq_sys_config
(config_key, config_value, config_name, config_group, value_type, is_builtin, remark, created_by, updated_by)
SELECT 'sys.login.captcha.enabled', 'false', '登录验证码开关', 'AUTH', 'boolean', 0, '预留登录验证码控制', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_config WHERE config_key = 'sys.login.captcha.enabled'
);

UPDATE jq_sys_config SET config_group = 'UI' WHERE config_key = 'sys.theme.default';
UPDATE jq_sys_config SET config_group = 'AUTH' WHERE config_key = 'sys.login.captcha.enabled';

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT menu.id, 3, '参数查询', '', '', 'system:config:query', '', 1, 1, 1, 0, 1, 1
FROM jq_sys_menu menu
WHERE menu.perms = 'system:config:list'
  AND menu.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'system:config:query' AND is_deleted = 0
  );

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT menu.id, 3, '参数新增', '', '', 'system:config:add', '', 2, 1, 1, 0, 1, 1
FROM jq_sys_menu menu
WHERE menu.perms = 'system:config:list'
  AND menu.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'system:config:add' AND is_deleted = 0
  );

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT menu.id, 3, '参数修改', '', '', 'system:config:edit', '', 3, 1, 1, 0, 1, 1
FROM jq_sys_menu menu
WHERE menu.perms = 'system:config:list'
  AND menu.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'system:config:edit' AND is_deleted = 0
  );

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT menu.id, 3, '参数删除', '', '', 'system:config:remove', '', 4, 1, 1, 0, 1, 1
FROM jq_sys_menu menu
WHERE menu.perms = 'system:config:list'
  AND menu.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'system:config:remove' AND is_deleted = 0
  );

INSERT INTO jq_sys_role_menu (role_id, menu_id)
SELECT 1, menu.id
FROM jq_sys_menu menu
WHERE menu.is_deleted = 0
  AND menu.perms IN ('system:config:list', 'system:config:query', 'system:config:add', 'system:config:edit', 'system:config:remove')
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = menu.id
  );

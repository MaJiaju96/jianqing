USE jianqing;

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT 0, 1, '审计日志', '/audit', 'Layout', '', 'Document', 20, 1, 1, 0, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_menu WHERE route_path = '/audit' AND menu_type = 1 AND is_deleted = 0
);

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT parent.id, 2, '操作日志', 'oper-logs', 'audit/oper/index', 'audit:oper-log:list', 'List', 1, 1, 1, 0, 1, 1
FROM jq_sys_menu parent
WHERE parent.route_path = '/audit'
  AND parent.menu_type = 1
  AND parent.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'audit:oper-log:list' AND is_deleted = 0
  )
LIMIT 1;

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT parent.id, 2, '登录日志', 'login-logs', 'audit/login/index', 'audit:login-log:list', 'Tickets', 2, 1, 1, 0, 1, 1
FROM jq_sys_menu parent
WHERE parent.route_path = '/audit'
  AND parent.menu_type = 1
  AND parent.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'audit:login-log:list' AND is_deleted = 0
  )
LIMIT 1;

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT menu.id, 3, '操作日志查询', '', '', 'audit:oper-log:query', '', 1, 1, 1, 0, 1, 1
FROM jq_sys_menu menu
WHERE menu.perms = 'audit:oper-log:list'
  AND menu.menu_type = 2
  AND menu.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'audit:oper-log:query' AND is_deleted = 0
  )
LIMIT 1;

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT menu.id, 3, '登录日志查询', '', '', 'audit:login-log:query', '', 1, 1, 1, 0, 1, 1
FROM jq_sys_menu menu
WHERE menu.perms = 'audit:login-log:list'
  AND menu.menu_type = 2
  AND menu.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'audit:login-log:query' AND is_deleted = 0
  )
LIMIT 1;

INSERT INTO jq_sys_role_menu (role_id, menu_id)
SELECT role.id, menu.id
FROM jq_sys_role role
JOIN jq_sys_menu menu ON menu.is_deleted = 0
LEFT JOIN jq_sys_role_menu rm ON rm.role_id = role.id AND rm.menu_id = menu.id
WHERE role.role_code = 'super_admin'
  AND role.is_deleted = 0
  AND rm.id IS NULL;

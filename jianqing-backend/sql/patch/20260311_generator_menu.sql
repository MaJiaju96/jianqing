INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT 1, 2, '代码生成', 'generator', 'system/generator/index', 'system:generator:list', 'MagicStick', 7, 1, 1, 0, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_menu WHERE perms = 'system:generator:list' AND is_deleted = 0
);

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT menu.id, 3, '代码生成查询', '', '', 'system:generator:query', '', 1, 1, 1, 0, 1, 1
FROM jq_sys_menu menu
WHERE menu.perms = 'system:generator:list'
  AND menu.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'system:generator:query' AND is_deleted = 0
  );

INSERT INTO jq_sys_role_menu (role_id, menu_id)
SELECT 1, menu.id
FROM jq_sys_menu menu
WHERE menu.is_deleted = 0
  AND menu.perms IN ('system:generator:list', 'system:generator:query')
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = menu.id
  );

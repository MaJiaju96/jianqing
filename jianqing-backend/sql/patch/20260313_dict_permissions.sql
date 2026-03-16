INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT 1, 2, '字典管理', 'dict', 'system/dict/index', 'system:dict:list', 'CollectionTag', 5, 1, 1, 0, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_menu WHERE perms = 'system:dict:list' AND is_deleted = 0
);

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT menu.id, 3, '字典查询', '', '', 'system:dict:query', '', 1, 1, 1, 0, 1, 1
FROM jq_sys_menu menu
WHERE menu.perms = 'system:dict:list'
  AND menu.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'system:dict:query' AND is_deleted = 0
  );

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT menu.id, 3, '字典新增', '', '', 'system:dict:add', '', 2, 1, 1, 0, 1, 1
FROM jq_sys_menu menu
WHERE menu.perms = 'system:dict:list'
  AND menu.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'system:dict:add' AND is_deleted = 0
  );

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT menu.id, 3, '字典修改', '', '', 'system:dict:edit', '', 3, 1, 1, 0, 1, 1
FROM jq_sys_menu menu
WHERE menu.perms = 'system:dict:list'
  AND menu.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'system:dict:edit' AND is_deleted = 0
  );

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT menu.id, 3, '字典删除', '', '', 'system:dict:remove', '', 4, 1, 1, 0, 1, 1
FROM jq_sys_menu menu
WHERE menu.perms = 'system:dict:list'
  AND menu.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'system:dict:remove' AND is_deleted = 0
  );

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT menu.id, 3, '字典数据新增', '', '', 'system:dict-data:add', '', 5, 1, 1, 0, 1, 1
FROM jq_sys_menu menu
WHERE menu.perms = 'system:dict:list'
  AND menu.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'system:dict-data:add' AND is_deleted = 0
  );

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT menu.id, 3, '字典数据修改', '', '', 'system:dict-data:edit', '', 6, 1, 1, 0, 1, 1
FROM jq_sys_menu menu
WHERE menu.perms = 'system:dict:list'
  AND menu.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'system:dict-data:edit' AND is_deleted = 0
  );

INSERT INTO jq_sys_menu
(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)
SELECT menu.id, 3, '字典数据删除', '', '', 'system:dict-data:remove', '', 7, 1, 1, 0, 1, 1
FROM jq_sys_menu menu
WHERE menu.perms = 'system:dict:list'
  AND menu.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_menu WHERE perms = 'system:dict-data:remove' AND is_deleted = 0
  );

INSERT INTO jq_sys_role_menu (role_id, menu_id)
SELECT 1, menu.id
FROM jq_sys_menu menu
WHERE menu.is_deleted = 0
  AND menu.perms IN (
      'system:dict:list',
      'system:dict:query',
      'system:dict:add',
      'system:dict:edit',
      'system:dict:remove',
      'system:dict-data:add',
      'system:dict-data:edit',
      'system:dict-data:remove'
  )
  AND NOT EXISTS (
      SELECT 1 FROM jq_sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = menu.id
  );

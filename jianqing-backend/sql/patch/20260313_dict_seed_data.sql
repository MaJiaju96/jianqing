INSERT INTO jq_sys_dict_type
(dict_name, dict_type, status, remark, created_by, updated_by)
SELECT '通用状态', 'sys_common_status', 1, '用户/角色/菜单通用启禁用状态', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_type WHERE dict_type = 'sys_common_status'
);

INSERT INTO jq_sys_dict_type
(dict_name, dict_type, status, remark, created_by, updated_by)
SELECT '菜单可见性', 'sys_menu_visible', 1, '菜单显示/隐藏状态', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_type WHERE dict_type = 'sys_menu_visible'
);

INSERT INTO jq_sys_dict_type
(dict_name, dict_type, status, remark, created_by, updated_by)
SELECT '部门状态', 'sys_dept_status', 1, '部门启用/停用状态', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_type WHERE dict_type = 'sys_dept_status'
);

INSERT INTO jq_sys_dict_data
(dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'sys_common_status', '启用', '1', 'success', '', 1, 1, '启用', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_common_status' AND value = '1'
);

INSERT INTO jq_sys_dict_data
(dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'sys_common_status', '禁用', '0', 'danger', '', 2, 1, '禁用', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_common_status' AND value = '0'
);

INSERT INTO jq_sys_dict_data
(dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'sys_menu_visible', '显示', '1', 'info', '', 1, 1, '菜单显示', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_menu_visible' AND value = '1'
);

INSERT INTO jq_sys_dict_data
(dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'sys_menu_visible', '隐藏', '0', 'warning', '', 2, 1, '菜单隐藏', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_menu_visible' AND value = '0'
);

INSERT INTO jq_sys_dict_data
(dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'sys_dept_status', '启用', '1', 'success', '', 1, 1, '部门启用', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_dept_status' AND value = '1'
);

INSERT INTO jq_sys_dict_data
(dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'sys_dept_status', '停用', '0', 'info', '', 2, 1, '部门停用', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_dept_status' AND value = '0'
);

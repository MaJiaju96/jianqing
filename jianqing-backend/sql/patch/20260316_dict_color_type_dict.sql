INSERT INTO jq_sys_dict_type (dict_name, dict_type, status, remark, created_by, updated_by)
SELECT '字典颜色类型', 'sys_dict_color_type', 1, '字典数据颜色类型选项', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_type WHERE dict_type = 'sys_dict_color_type'
);

INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'sys_dict_color_type', '默认', '', '', '', 1, 1, '默认颜色类型', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_dict_color_type' AND value = ''
);

INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'sys_dict_color_type', '主色', 'primary', 'primary', '', 2, 1, '主色标签', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_dict_color_type' AND value = 'primary'
);

INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'sys_dict_color_type', '成功', 'success', 'success', '', 3, 1, '成功标签', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_dict_color_type' AND value = 'success'
);

INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'sys_dict_color_type', '信息', 'info', 'info', '', 4, 1, '信息标签', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_dict_color_type' AND value = 'info'
);

INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'sys_dict_color_type', '警告', 'warning', 'warning', '', 5, 1, '警告标签', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_dict_color_type' AND value = 'warning'
);

INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'sys_dict_color_type', '危险', 'danger', 'danger', '', 6, 1, '危险标签', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_dict_color_type' AND value = 'danger'
);

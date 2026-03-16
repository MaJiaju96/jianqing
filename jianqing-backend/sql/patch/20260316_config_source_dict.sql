INSERT INTO jq_sys_dict_type (dict_name, dict_type, status, remark, created_by, updated_by)
SELECT '参数来源', 'sys_config_source', 1, '系统参数来源选项', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_type WHERE dict_type = 'sys_config_source'
);

INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'sys_config_source', '自定义', '0', 'info', '', 1, 1, '自定义参数', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_config_source' AND value = '0'
);

INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'sys_config_source', '内置', '1', 'warning', '', 2, 1, '内置参数', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_config_source' AND value = '1'
);

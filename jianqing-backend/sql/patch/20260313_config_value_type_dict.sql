INSERT INTO jq_sys_dict_type (dict_name, dict_type, status, remark, created_by, updated_by)
SELECT '参数值类型', 'sys_config_value_type', 1, '系统参数值类型选项', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_type WHERE dict_type = 'sys_config_value_type'
);

INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort, status, remark, created_by, updated_by)
SELECT 'sys_config_value_type', '字符串', 'string', 'info', '', 1, 1, '字符串参数值', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_config_value_type' AND value = 'string'
);

INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort, status, remark, created_by, updated_by)
SELECT 'sys_config_value_type', '数字', 'number', 'warning', '', 2, 1, '数字参数值', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_config_value_type' AND value = 'number'
);

INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort, status, remark, created_by, updated_by)
SELECT 'sys_config_value_type', '布尔值', 'boolean', 'success', '', 3, 1, '布尔参数值', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_config_value_type' AND value = 'boolean'
);

INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort, status, remark, created_by, updated_by)
SELECT 'sys_config_value_type', 'JSON', 'json', 'primary', '', 4, 1, 'JSON 参数值', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'sys_config_value_type' AND value = 'json'
);

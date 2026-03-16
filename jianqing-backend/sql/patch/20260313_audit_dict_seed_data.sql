INSERT INTO jq_sys_dict_type
(dict_name, dict_type, status, remark, created_by, updated_by)
SELECT '审计执行状态', 'audit_exec_status', 1, '审计日志成功/失败状态', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_type WHERE dict_type = 'audit_exec_status'
);

INSERT INTO jq_sys_dict_type
(dict_name, dict_type, status, remark, created_by, updated_by)
SELECT '登录方式', 'audit_login_type', 1, '登录日志登录方式', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_type WHERE dict_type = 'audit_login_type'
);

INSERT INTO jq_sys_dict_data
(dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'audit_exec_status', '成功', '1', 'success', '', 1, 1, '审计成功', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'audit_exec_status' AND value = '1'
);

INSERT INTO jq_sys_dict_data
(dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'audit_exec_status', '失败', '0', 'danger', '', 2, 1, '审计失败', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'audit_exec_status' AND value = '0'
);

INSERT INTO jq_sys_dict_data
(dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by)
SELECT 'audit_login_type', '密码', 'password', 'info', '', 1, 1, '密码登录', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM jq_sys_dict_data WHERE dict_type = 'audit_login_type' AND value = 'password'
);

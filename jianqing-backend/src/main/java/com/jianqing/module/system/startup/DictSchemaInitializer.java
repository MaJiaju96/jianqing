package com.jianqing.module.system.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DictSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DictSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String schemaName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (schemaName == null || schemaName.trim().isEmpty()) {
            return;
        }
        ensureDictTypeTable(schemaName);
        ensureDictDataTable(schemaName);
        ensureDictTypeColumns(schemaName);
        ensureDictDataColumns(schemaName);
        ensureBuiltInDicts();
    }

    private void ensureBuiltInDicts() {
        ensureDictTypeSeed("通用状态", "sys_common_status", "用户/角色/菜单通用启禁用状态");
        ensureDictTypeSeed("部门状态", "sys_dept_status", "部门启用/禁用状态");
        ensureDictDataSeed("sys_common_status", "启用", "1", "success", 1, "启用");
        ensureDictDataSeed("sys_common_status", "禁用", "0", "danger", 2, "禁用");
        ensureDictDataSeed("sys_dept_status", "启用", "1", "success", 1, "部门启用");
        ensureDictDataSeed("sys_dept_status", "禁用", "0", "info", 2, "部门禁用");
    }

    private void ensureDictTypeTable(String schemaName) {
        if (tableExists(schemaName, "jq_sys_dict_type")) {
            return;
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS jq_sys_dict_type ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "dict_name VARCHAR(64) NOT NULL,"
                + "dict_type VARCHAR(64) NOT NULL,"
                + "status TINYINT NOT NULL DEFAULT 1,"
                + "remark VARCHAR(255) NOT NULL DEFAULT '',"
                + "created_by BIGINT NOT NULL DEFAULT 0,"
                + "updated_by BIGINT NOT NULL DEFAULT 0,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uk_jq_sys_dict_type_type (dict_type)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void ensureDictDataTable(String schemaName) {
        if (tableExists(schemaName, "jq_sys_dict_data")) {
            return;
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS jq_sys_dict_data ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "dict_type VARCHAR(64) NOT NULL,"
                + "label VARCHAR(64) NOT NULL,"
                + "value VARCHAR(64) NOT NULL,"
                + "color_type VARCHAR(32) NOT NULL DEFAULT '',"
                + "css_class VARCHAR(64) NOT NULL DEFAULT '',"
                + "sort_no INT NOT NULL DEFAULT 0,"
                + "status TINYINT NOT NULL DEFAULT 1,"
                + "remark VARCHAR(255) NOT NULL DEFAULT '',"
                + "created_by BIGINT NOT NULL DEFAULT 0,"
                + "updated_by BIGINT NOT NULL DEFAULT 0,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uk_jq_sys_dict_data_type_value (dict_type, value),"
                + "KEY idx_jq_sys_dict_data_type (dict_type)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void ensureDictTypeColumns(String schemaName) {
        ensureColumnExists(schemaName, "jq_sys_dict_type", "status",
                "ALTER TABLE jq_sys_dict_type ADD COLUMN status TINYINT NOT NULL DEFAULT 1 AFTER dict_type");
        ensureColumnExists(schemaName, "jq_sys_dict_type", "remark",
                "ALTER TABLE jq_sys_dict_type ADD COLUMN remark VARCHAR(255) NOT NULL DEFAULT '' AFTER status");
        ensureColumnExists(schemaName, "jq_sys_dict_type", "created_by",
                "ALTER TABLE jq_sys_dict_type ADD COLUMN created_by BIGINT NOT NULL DEFAULT 0 AFTER remark");
        ensureColumnExists(schemaName, "jq_sys_dict_type", "updated_by",
                "ALTER TABLE jq_sys_dict_type ADD COLUMN updated_by BIGINT NOT NULL DEFAULT 0 AFTER created_by");
        ensureColumnExists(schemaName, "jq_sys_dict_type", "created_at",
                "ALTER TABLE jq_sys_dict_type ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER updated_by");
        ensureColumnExists(schemaName, "jq_sys_dict_type", "updated_at",
                "ALTER TABLE jq_sys_dict_type ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at");
    }

    private void ensureDictDataColumns(String schemaName) {
        ensureColumnExists(schemaName, "jq_sys_dict_data", "color_type",
                "ALTER TABLE jq_sys_dict_data ADD COLUMN color_type VARCHAR(32) NOT NULL DEFAULT '' AFTER value");
        ensureColumnExists(schemaName, "jq_sys_dict_data", "css_class",
                "ALTER TABLE jq_sys_dict_data ADD COLUMN css_class VARCHAR(64) NOT NULL DEFAULT '' AFTER color_type");
        ensureColumnExists(schemaName, "jq_sys_dict_data", "sort_no",
                "ALTER TABLE jq_sys_dict_data ADD COLUMN sort_no INT NOT NULL DEFAULT 0 AFTER css_class");
        ensureColumnExists(schemaName, "jq_sys_dict_data", "status",
                "ALTER TABLE jq_sys_dict_data ADD COLUMN status TINYINT NOT NULL DEFAULT 1 AFTER sort_no");
        ensureColumnExists(schemaName, "jq_sys_dict_data", "remark",
                "ALTER TABLE jq_sys_dict_data ADD COLUMN remark VARCHAR(255) NOT NULL DEFAULT '' AFTER status");
        ensureColumnExists(schemaName, "jq_sys_dict_data", "created_by",
                "ALTER TABLE jq_sys_dict_data ADD COLUMN created_by BIGINT NOT NULL DEFAULT 0 AFTER remark");
        ensureColumnExists(schemaName, "jq_sys_dict_data", "updated_by",
                "ALTER TABLE jq_sys_dict_data ADD COLUMN updated_by BIGINT NOT NULL DEFAULT 0 AFTER created_by");
        ensureColumnExists(schemaName, "jq_sys_dict_data", "created_at",
                "ALTER TABLE jq_sys_dict_data ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER updated_by");
        ensureColumnExists(schemaName, "jq_sys_dict_data", "updated_at",
                "ALTER TABLE jq_sys_dict_data ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at");
    }

    private boolean tableExists(String schemaName, String tableName) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?",
                Integer.class, schemaName, tableName);
        return exists != null && exists > 0;
    }

    private void ensureColumnExists(String schemaName, String tableName, String columnName, String alterSql) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?",
                Integer.class, schemaName, tableName, columnName);
        if (exists != null && exists > 0) {
            return;
        }
        jdbcTemplate.execute(alterSql);
    }

    private void ensureDictTypeSeed(String dictName, String dictType, String remark) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM jq_sys_dict_type WHERE dict_type = ?",
                Integer.class, dictType);
        if (exists != null && exists > 0) {
            return;
        }
        jdbcTemplate.update("INSERT INTO jq_sys_dict_type (dict_name, dict_type, status, remark, created_by, updated_by) VALUES (?, ?, 1, ?, 1, 1)",
                dictName, dictType, remark);
    }

    private void ensureDictDataSeed(String dictType, String label, String value, String colorType, int sortNo, String remark) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM jq_sys_dict_data WHERE dict_type = ? AND value = ?",
                Integer.class, dictType, value);
        if (exists != null && exists > 0) {
            return;
        }
        jdbcTemplate.update("INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by) VALUES (?, ?, ?, ?, '', ?, 1, ?, 1, 1)",
                dictType, label, value, colorType, sortNo, remark);
    }
}

package com.jianqing.module.system.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeptSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DeptSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String schemaName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (schemaName == null || schemaName.trim().isEmpty()) {
            return;
        }
        ensureDeptTable(schemaName);
        ensureDeptColumns(schemaName);
    }

    private void ensureDeptTable(String schemaName) {
        if (tableExists(schemaName, "jq_sys_dept")) {
            return;
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS jq_sys_dept ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "parent_id BIGINT NOT NULL DEFAULT 0,"
                + "dept_name VARCHAR(64) NOT NULL,"
                + "leader_user_id BIGINT NOT NULL DEFAULT 0,"
                + "phone VARCHAR(20) NOT NULL DEFAULT '',"
                + "email VARCHAR(128) NOT NULL DEFAULT '',"
                + "sort_no INT NOT NULL DEFAULT 0,"
                + "status TINYINT NOT NULL DEFAULT 1,"
                + "is_deleted TINYINT NOT NULL DEFAULT 0,"
                + "created_by BIGINT NOT NULL DEFAULT 0,"
                + "updated_by BIGINT NOT NULL DEFAULT 0,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "KEY idx_jq_sys_dept_parent_id (parent_id),"
                + "KEY idx_jq_sys_dept_status (status)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void ensureDeptColumns(String schemaName) {
        ensureColumnExists(schemaName, "jq_sys_dept", "leader_user_id",
                "ALTER TABLE jq_sys_dept ADD COLUMN leader_user_id BIGINT NOT NULL DEFAULT 0 AFTER dept_name");
        ensureColumnExists(schemaName, "jq_sys_dept", "phone",
                "ALTER TABLE jq_sys_dept ADD COLUMN phone VARCHAR(20) NOT NULL DEFAULT '' AFTER leader_user_id");
        ensureColumnExists(schemaName, "jq_sys_dept", "email",
                "ALTER TABLE jq_sys_dept ADD COLUMN email VARCHAR(128) NOT NULL DEFAULT '' AFTER phone");
        ensureColumnExists(schemaName, "jq_sys_dept", "sort_no",
                "ALTER TABLE jq_sys_dept ADD COLUMN sort_no INT NOT NULL DEFAULT 0 AFTER email");
        ensureColumnExists(schemaName, "jq_sys_dept", "status",
                "ALTER TABLE jq_sys_dept ADD COLUMN status TINYINT NOT NULL DEFAULT 1 AFTER sort_no");
        ensureColumnExists(schemaName, "jq_sys_dept", "is_deleted",
                "ALTER TABLE jq_sys_dept ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 AFTER status");
        ensureColumnExists(schemaName, "jq_sys_dept", "created_by",
                "ALTER TABLE jq_sys_dept ADD COLUMN created_by BIGINT NOT NULL DEFAULT 0 AFTER is_deleted");
        ensureColumnExists(schemaName, "jq_sys_dept", "updated_by",
                "ALTER TABLE jq_sys_dept ADD COLUMN updated_by BIGINT NOT NULL DEFAULT 0 AFTER created_by");
        ensureColumnExists(schemaName, "jq_sys_dept", "created_at",
                "ALTER TABLE jq_sys_dept ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER updated_by");
        ensureColumnExists(schemaName, "jq_sys_dept", "updated_at",
                "ALTER TABLE jq_sys_dept ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at");
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
}

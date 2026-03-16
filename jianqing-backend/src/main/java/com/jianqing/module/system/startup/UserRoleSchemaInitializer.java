package com.jianqing.module.system.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserRoleSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public UserRoleSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String schemaName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (schemaName == null || schemaName.trim().isEmpty()) {
            return;
        }
        ensureUserTable(schemaName);
        ensureRoleTable(schemaName);
        ensureUserRoleTable(schemaName);
        ensureUserColumns(schemaName);
        ensureRoleColumns(schemaName);
    }

    private void ensureUserTable(String schemaName) {
        if (tableExists(schemaName, "jq_sys_user")) {
            return;
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS jq_sys_user ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "username VARCHAR(64) NOT NULL,"
                + "password_hash VARCHAR(255) NOT NULL,"
                + "nickname VARCHAR(64) NOT NULL DEFAULT '',"
                + "real_name VARCHAR(64) NOT NULL DEFAULT '',"
                + "mobile VARCHAR(20) NOT NULL DEFAULT '',"
                + "email VARCHAR(128) NOT NULL DEFAULT '',"
                + "dept_id BIGINT NOT NULL DEFAULT 0,"
                + "status TINYINT NOT NULL DEFAULT 1,"
                + "is_deleted TINYINT NOT NULL DEFAULT 0,"
                + "last_login_ip VARCHAR(64) NOT NULL DEFAULT '',"
                + "last_login_time DATETIME NULL,"
                + "remark VARCHAR(255) NOT NULL DEFAULT '',"
                + "created_by BIGINT NOT NULL DEFAULT 0,"
                + "updated_by BIGINT NOT NULL DEFAULT 0,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uk_jq_sys_user_username (username),"
                + "KEY idx_jq_sys_user_dept_id (dept_id),"
                + "KEY idx_jq_sys_user_status (status)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void ensureRoleTable(String schemaName) {
        if (tableExists(schemaName, "jq_sys_role")) {
            return;
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS jq_sys_role ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "role_name VARCHAR(64) NOT NULL,"
                + "role_code VARCHAR(64) NOT NULL,"
                + "data_scope TINYINT NOT NULL DEFAULT 1,"
                + "status TINYINT NOT NULL DEFAULT 1,"
                + "is_deleted TINYINT NOT NULL DEFAULT 0,"
                + "remark VARCHAR(255) NOT NULL DEFAULT '',"
                + "created_by BIGINT NOT NULL DEFAULT 0,"
                + "updated_by BIGINT NOT NULL DEFAULT 0,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uk_jq_sys_role_role_code (role_code),"
                + "KEY idx_jq_sys_role_status (status)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void ensureUserRoleTable(String schemaName) {
        if (tableExists(schemaName, "jq_sys_user_role")) {
            return;
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS jq_sys_user_role ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "user_id BIGINT NOT NULL,"
                + "role_id BIGINT NOT NULL,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uk_jq_sys_user_role (user_id, role_id),"
                + "KEY idx_jq_sys_user_role_role_id (role_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void ensureUserColumns(String schemaName) {
        ensureColumnExists(schemaName, "jq_sys_user", "mobile",
                "ALTER TABLE jq_sys_user ADD COLUMN mobile VARCHAR(20) NOT NULL DEFAULT '' AFTER real_name");
        ensureColumnExists(schemaName, "jq_sys_user", "email",
                "ALTER TABLE jq_sys_user ADD COLUMN email VARCHAR(128) NOT NULL DEFAULT '' AFTER mobile");
        ensureColumnExists(schemaName, "jq_sys_user", "dept_id",
                "ALTER TABLE jq_sys_user ADD COLUMN dept_id BIGINT NOT NULL DEFAULT 0 AFTER email");
        ensureColumnExists(schemaName, "jq_sys_user", "status",
                "ALTER TABLE jq_sys_user ADD COLUMN status TINYINT NOT NULL DEFAULT 1 AFTER dept_id");
        ensureColumnExists(schemaName, "jq_sys_user", "is_deleted",
                "ALTER TABLE jq_sys_user ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 AFTER status");
        ensureColumnExists(schemaName, "jq_sys_user", "last_login_ip",
                "ALTER TABLE jq_sys_user ADD COLUMN last_login_ip VARCHAR(64) NOT NULL DEFAULT '' AFTER is_deleted");
        ensureColumnExists(schemaName, "jq_sys_user", "last_login_time",
                "ALTER TABLE jq_sys_user ADD COLUMN last_login_time DATETIME NULL AFTER last_login_ip");
        ensureColumnExists(schemaName, "jq_sys_user", "remark",
                "ALTER TABLE jq_sys_user ADD COLUMN remark VARCHAR(255) NOT NULL DEFAULT '' AFTER last_login_time");
        ensureColumnExists(schemaName, "jq_sys_user", "created_by",
                "ALTER TABLE jq_sys_user ADD COLUMN created_by BIGINT NOT NULL DEFAULT 0 AFTER remark");
        ensureColumnExists(schemaName, "jq_sys_user", "updated_by",
                "ALTER TABLE jq_sys_user ADD COLUMN updated_by BIGINT NOT NULL DEFAULT 0 AFTER created_by");
        ensureColumnExists(schemaName, "jq_sys_user", "created_at",
                "ALTER TABLE jq_sys_user ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER updated_by");
        ensureColumnExists(schemaName, "jq_sys_user", "updated_at",
                "ALTER TABLE jq_sys_user ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at");
    }

    private void ensureRoleColumns(String schemaName) {
        ensureColumnExists(schemaName, "jq_sys_role", "data_scope",
                "ALTER TABLE jq_sys_role ADD COLUMN data_scope TINYINT NOT NULL DEFAULT 1 AFTER role_code");
        ensureColumnExists(schemaName, "jq_sys_role", "status",
                "ALTER TABLE jq_sys_role ADD COLUMN status TINYINT NOT NULL DEFAULT 1 AFTER data_scope");
        ensureColumnExists(schemaName, "jq_sys_role", "is_deleted",
                "ALTER TABLE jq_sys_role ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 AFTER status");
        ensureColumnExists(schemaName, "jq_sys_role", "remark",
                "ALTER TABLE jq_sys_role ADD COLUMN remark VARCHAR(255) NOT NULL DEFAULT '' AFTER is_deleted");
        ensureColumnExists(schemaName, "jq_sys_role", "created_by",
                "ALTER TABLE jq_sys_role ADD COLUMN created_by BIGINT NOT NULL DEFAULT 0 AFTER remark");
        ensureColumnExists(schemaName, "jq_sys_role", "updated_by",
                "ALTER TABLE jq_sys_role ADD COLUMN updated_by BIGINT NOT NULL DEFAULT 0 AFTER created_by");
        ensureColumnExists(schemaName, "jq_sys_role", "created_at",
                "ALTER TABLE jq_sys_role ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER updated_by");
        ensureColumnExists(schemaName, "jq_sys_role", "updated_at",
                "ALTER TABLE jq_sys_role ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at");
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

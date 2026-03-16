package com.jianqing.module.system.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MenuSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public MenuSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String schemaName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (schemaName == null || schemaName.trim().isEmpty()) {
            return;
        }
        ensureMenuCoreColumns(schemaName);
        ensureRoleMenuTable(schemaName);
    }

    private void ensureMenuCoreColumns(String schemaName) {
        ensureColumnExists(schemaName, "jq_sys_menu", "route_path",
                "ALTER TABLE jq_sys_menu ADD COLUMN route_path VARCHAR(128) NOT NULL DEFAULT '' AFTER menu_name");
        ensureColumnExists(schemaName, "jq_sys_menu", "component",
                "ALTER TABLE jq_sys_menu ADD COLUMN component VARCHAR(255) NOT NULL DEFAULT '' AFTER route_path");
        ensureColumnExists(schemaName, "jq_sys_menu", "perms",
                "ALTER TABLE jq_sys_menu ADD COLUMN perms VARCHAR(128) NOT NULL DEFAULT '' AFTER component");
        ensureColumnExists(schemaName, "jq_sys_menu", "icon",
                "ALTER TABLE jq_sys_menu ADD COLUMN icon VARCHAR(64) NOT NULL DEFAULT '' AFTER perms");
        ensureColumnExists(schemaName, "jq_sys_menu", "sort_no",
                "ALTER TABLE jq_sys_menu ADD COLUMN sort_no INT NOT NULL DEFAULT 0 AFTER icon");
        ensureColumnExists(schemaName, "jq_sys_menu", "visible",
                "ALTER TABLE jq_sys_menu ADD COLUMN visible TINYINT NOT NULL DEFAULT 1 AFTER sort_no");
        ensureColumnExists(schemaName, "jq_sys_menu", "status",
                "ALTER TABLE jq_sys_menu ADD COLUMN status TINYINT NOT NULL DEFAULT 1 AFTER visible");
        ensureColumnExists(schemaName, "jq_sys_menu", "is_deleted",
                "ALTER TABLE jq_sys_menu ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 AFTER status");
        ensureColumnExists(schemaName, "jq_sys_menu", "created_by",
                "ALTER TABLE jq_sys_menu ADD COLUMN created_by BIGINT NOT NULL DEFAULT 0 AFTER is_deleted");
        ensureColumnExists(schemaName, "jq_sys_menu", "updated_by",
                "ALTER TABLE jq_sys_menu ADD COLUMN updated_by BIGINT NOT NULL DEFAULT 0 AFTER created_by");
        ensureColumnExists(schemaName, "jq_sys_menu", "created_at",
                "ALTER TABLE jq_sys_menu ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER updated_by");
        ensureColumnExists(schemaName, "jq_sys_menu", "updated_at",
                "ALTER TABLE jq_sys_menu ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at");
    }

    private void ensureRoleMenuTable(String schemaName) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?",
                Integer.class, schemaName, "jq_sys_role_menu");
        if (exists != null && exists > 0) {
            return;
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS jq_sys_role_menu ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "role_id BIGINT NOT NULL,"
                + "menu_id BIGINT NOT NULL,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uk_jq_sys_role_menu (role_id, menu_id),"
                + "KEY idx_jq_sys_role_menu_menu_id (menu_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
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

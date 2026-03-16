package com.jianqing.module.system.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ConfigSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public ConfigSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String schemaName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (schemaName == null || schemaName.trim().isEmpty()) {
            return;
        }
        ensureConfigColumns(schemaName);
        ensureConfigHistoryTable(schemaName);
        ensureConfigHistoryColumns(schemaName);
    }

    private void ensureConfigColumns(String schemaName) {
        ensureColumnExists(schemaName, "jq_sys_config", "config_group",
                "ALTER TABLE jq_sys_config ADD COLUMN config_group VARCHAR(64) NOT NULL DEFAULT 'DEFAULT_GROUP' AFTER config_name");
        ensureColumnExists(schemaName, "jq_sys_config", "value_type",
                "ALTER TABLE jq_sys_config ADD COLUMN value_type VARCHAR(32) NOT NULL DEFAULT 'string' AFTER config_group");
        ensureColumnExists(schemaName, "jq_sys_config", "is_builtin",
                "ALTER TABLE jq_sys_config ADD COLUMN is_builtin TINYINT NOT NULL DEFAULT 0 AFTER value_type");
    }

    private void ensureConfigHistoryTable(String schemaName) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?",
                Integer.class, schemaName, "jq_sys_config_history");
        if (exists != null && exists > 0) {
            return;
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS jq_sys_config_history ("
                + "id BIGINT NOT NULL AUTO_INCREMENT,"
                + "config_id BIGINT NOT NULL,"
                + "config_key VARCHAR(128) NOT NULL,"
                + "config_name VARCHAR(128) NOT NULL,"
                + "config_group VARCHAR(64) NOT NULL DEFAULT 'DEFAULT_GROUP',"
                + "config_value VARCHAR(1024) NOT NULL,"
                + "value_type VARCHAR(32) NOT NULL DEFAULT 'string',"
                + "is_builtin TINYINT NOT NULL DEFAULT 0,"
                + "change_type VARCHAR(32) NOT NULL,"
                + "change_note VARCHAR(255) NOT NULL DEFAULT '',"
                + "created_by BIGINT NOT NULL DEFAULT 0,"
                + "updated_by BIGINT NOT NULL DEFAULT 0,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "PRIMARY KEY (id),"
                + "KEY idx_jq_sys_config_history_config_id (config_id),"
                + "KEY idx_jq_sys_config_history_created_at (created_at)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
    }

    private void ensureConfigHistoryColumns(String schemaName) {
        ensureColumnExists(schemaName, "jq_sys_config_history", "config_group",
                "ALTER TABLE jq_sys_config_history ADD COLUMN config_group VARCHAR(64) NOT NULL DEFAULT 'DEFAULT_GROUP' AFTER config_name");
        ensureColumnExists(schemaName, "jq_sys_config_history", "value_type",
                "ALTER TABLE jq_sys_config_history ADD COLUMN value_type VARCHAR(32) NOT NULL DEFAULT 'string' AFTER config_value");
        ensureColumnExists(schemaName, "jq_sys_config_history", "is_builtin",
                "ALTER TABLE jq_sys_config_history ADD COLUMN is_builtin TINYINT NOT NULL DEFAULT 0 AFTER value_type");
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

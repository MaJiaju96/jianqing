package com.jianqing.module.audit.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuditSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public AuditSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String schemaName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (schemaName == null || schemaName.trim().isEmpty()) {
            return;
        }
        ensureOperLogTable(schemaName);
        ensureLoginLogTable(schemaName);
        ensureOperLogColumns(schemaName);
        ensureLoginLogColumns(schemaName);
    }

    private void ensureOperLogTable(String schemaName) {
        if (tableExists(schemaName, "jq_sys_oper_log")) {
            return;
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS jq_sys_oper_log ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "trace_id VARCHAR(64) NOT NULL DEFAULT '',"
                + "user_id BIGINT NOT NULL DEFAULT 0,"
                + "username VARCHAR(64) NOT NULL DEFAULT '',"
                + "module_name VARCHAR(64) NOT NULL DEFAULT '',"
                + "biz_type VARCHAR(32) NOT NULL DEFAULT '',"
                + "method VARCHAR(255) NOT NULL DEFAULT '',"
                + "request_uri VARCHAR(255) NOT NULL DEFAULT '',"
                + "request_ip VARCHAR(64) NOT NULL DEFAULT '',"
                + "request_param LONGTEXT NULL,"
                + "response_data LONGTEXT NULL,"
                + "status TINYINT NOT NULL DEFAULT 1,"
                + "error_msg VARCHAR(1000) NOT NULL DEFAULT '',"
                + "cost_ms INT NOT NULL DEFAULT 0,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "KEY idx_jq_sys_oper_log_user_id (user_id),"
                + "KEY idx_jq_sys_oper_log_created_at (created_at),"
                + "KEY idx_jq_sys_oper_log_status (status)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void ensureLoginLogTable(String schemaName) {
        if (tableExists(schemaName, "jq_sys_login_log")) {
            return;
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS jq_sys_login_log ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "user_id BIGINT NOT NULL DEFAULT 0,"
                + "username VARCHAR(64) NOT NULL DEFAULT '',"
                + "login_type VARCHAR(32) NOT NULL DEFAULT 'password',"
                + "login_ip VARCHAR(64) NOT NULL DEFAULT '',"
                + "login_location VARCHAR(128) NOT NULL DEFAULT '',"
                + "user_agent VARCHAR(512) NOT NULL DEFAULT '',"
                + "os VARCHAR(64) NOT NULL DEFAULT '',"
                + "browser VARCHAR(64) NOT NULL DEFAULT '',"
                + "status TINYINT NOT NULL DEFAULT 1,"
                + "msg VARCHAR(255) NOT NULL DEFAULT '',"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "KEY idx_jq_sys_login_log_user_id (user_id),"
                + "KEY idx_jq_sys_login_log_created_at (created_at),"
                + "KEY idx_jq_sys_login_log_status (status)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void ensureOperLogColumns(String schemaName) {
        ensureColumnExists(schemaName, "jq_sys_oper_log", "request_param",
                "ALTER TABLE jq_sys_oper_log ADD COLUMN request_param LONGTEXT NULL AFTER request_ip");
        ensureColumnExists(schemaName, "jq_sys_oper_log", "response_data",
                "ALTER TABLE jq_sys_oper_log ADD COLUMN response_data LONGTEXT NULL AFTER request_param");
        ensureColumnExists(schemaName, "jq_sys_oper_log", "status",
                "ALTER TABLE jq_sys_oper_log ADD COLUMN status TINYINT NOT NULL DEFAULT 1 AFTER response_data");
        ensureColumnExists(schemaName, "jq_sys_oper_log", "error_msg",
                "ALTER TABLE jq_sys_oper_log ADD COLUMN error_msg VARCHAR(1000) NOT NULL DEFAULT '' AFTER status");
        ensureColumnExists(schemaName, "jq_sys_oper_log", "cost_ms",
                "ALTER TABLE jq_sys_oper_log ADD COLUMN cost_ms INT NOT NULL DEFAULT 0 AFTER error_msg");
        ensureColumnExists(schemaName, "jq_sys_oper_log", "created_at",
                "ALTER TABLE jq_sys_oper_log ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER cost_ms");
    }

    private void ensureLoginLogColumns(String schemaName) {
        ensureColumnExists(schemaName, "jq_sys_login_log", "login_type",
                "ALTER TABLE jq_sys_login_log ADD COLUMN login_type VARCHAR(32) NOT NULL DEFAULT 'password' AFTER username");
        ensureColumnExists(schemaName, "jq_sys_login_log", "login_ip",
                "ALTER TABLE jq_sys_login_log ADD COLUMN login_ip VARCHAR(64) NOT NULL DEFAULT '' AFTER login_type");
        ensureColumnExists(schemaName, "jq_sys_login_log", "login_location",
                "ALTER TABLE jq_sys_login_log ADD COLUMN login_location VARCHAR(128) NOT NULL DEFAULT '' AFTER login_ip");
        ensureColumnExists(schemaName, "jq_sys_login_log", "user_agent",
                "ALTER TABLE jq_sys_login_log ADD COLUMN user_agent VARCHAR(512) NOT NULL DEFAULT '' AFTER login_location");
        ensureColumnExists(schemaName, "jq_sys_login_log", "os",
                "ALTER TABLE jq_sys_login_log ADD COLUMN os VARCHAR(64) NOT NULL DEFAULT '' AFTER user_agent");
        ensureColumnExists(schemaName, "jq_sys_login_log", "browser",
                "ALTER TABLE jq_sys_login_log ADD COLUMN browser VARCHAR(64) NOT NULL DEFAULT '' AFTER os");
        ensureColumnExists(schemaName, "jq_sys_login_log", "status",
                "ALTER TABLE jq_sys_login_log ADD COLUMN status TINYINT NOT NULL DEFAULT 1 AFTER browser");
        ensureColumnExists(schemaName, "jq_sys_login_log", "msg",
                "ALTER TABLE jq_sys_login_log ADD COLUMN msg VARCHAR(255) NOT NULL DEFAULT '' AFTER status");
        ensureColumnExists(schemaName, "jq_sys_login_log", "created_at",
                "ALTER TABLE jq_sys_login_log ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER msg");
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

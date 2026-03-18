package com.jianqing.module.system.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class NoticeSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public NoticeSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String schemaName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (schemaName == null || schemaName.trim().isEmpty()) {
            return;
        }
        ensureNoticeTable(schemaName);
        ensureNoticeColumns(schemaName);
        normalizeNoticeColumns();
        ensureNoticeTargetTable(schemaName);
        ensureNoticeTargetColumns(schemaName);
        normalizeNoticeTargetColumns();
        ensureNoticeUserTable(schemaName);
        ensureNoticeUserColumns(schemaName);
        normalizeNoticeUserColumns();
    }

    private void ensureNoticeTable(String schemaName) {
        if (tableExists(schemaName, "jq_sys_notice")) {
            return;
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS jq_sys_notice ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "title VARCHAR(128) NOT NULL,"
                + "content TEXT NOT NULL,"
                + "level VARCHAR(32) NOT NULL DEFAULT 'NORMAL',"
                + "publish_mode VARCHAR(32) NOT NULL DEFAULT 'IMMEDIATE',"
                + "scheduled_at DATETIME NULL,"
                + "popup_enabled TINYINT NOT NULL DEFAULT 0,"
                + "valid_from DATETIME NULL,"
                + "valid_to DATETIME NULL,"
                + "target_type VARCHAR(32) NOT NULL DEFAULT 'ALL',"
                + "status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',"
                + "published_at DATETIME NULL,"
                + "remark VARCHAR(255) NOT NULL DEFAULT '',"
                + "created_by BIGINT NOT NULL DEFAULT 0,"
                + "updated_by BIGINT NOT NULL DEFAULT 0,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "KEY idx_jq_sys_notice_status_schedule (status, scheduled_at),"
                + "KEY idx_jq_sys_notice_published_at (published_at)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void ensureNoticeTargetTable(String schemaName) {
        if (tableExists(schemaName, "jq_sys_notice_target")) {
            return;
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS jq_sys_notice_target ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "notice_id BIGINT NOT NULL,"
                + "target_type VARCHAR(32) NOT NULL DEFAULT 'ALL',"
                + "target_id BIGINT NOT NULL DEFAULT 0,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "KEY idx_jq_sys_notice_target_notice_id (notice_id),"
                + "KEY idx_jq_sys_notice_target_type_id (target_type, target_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void ensureNoticeUserTable(String schemaName) {
        if (tableExists(schemaName, "jq_sys_notice_user")) {
            return;
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS jq_sys_notice_user ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "notice_id BIGINT NOT NULL,"
                + "user_id BIGINT NOT NULL,"
                + "read_status TINYINT NOT NULL DEFAULT 0,"
                + "read_at DATETIME NULL,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uk_jq_sys_notice_user_notice_user (notice_id, user_id),"
                + "KEY idx_jq_sys_notice_user_user_id (user_id, read_status),"
                + "KEY idx_jq_sys_notice_user_notice_id (notice_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void ensureNoticeColumns(String schemaName) {
        ensureColumnExists(schemaName, "jq_sys_notice", "title",
                "ALTER TABLE jq_sys_notice ADD COLUMN title VARCHAR(128) NOT NULL DEFAULT '' AFTER id");
        ensureColumnExists(schemaName, "jq_sys_notice", "content",
                "ALTER TABLE jq_sys_notice ADD COLUMN content TEXT NULL AFTER title");
        ensureColumnExists(schemaName, "jq_sys_notice", "level",
                "ALTER TABLE jq_sys_notice ADD COLUMN level VARCHAR(32) NOT NULL DEFAULT 'NORMAL' AFTER content");
        ensureColumnExists(schemaName, "jq_sys_notice", "publish_mode",
                "ALTER TABLE jq_sys_notice ADD COLUMN publish_mode VARCHAR(32) NOT NULL DEFAULT 'IMMEDIATE' AFTER level");
        ensureColumnExists(schemaName, "jq_sys_notice", "scheduled_at",
                "ALTER TABLE jq_sys_notice ADD COLUMN scheduled_at DATETIME NULL AFTER publish_mode");
        ensureColumnExists(schemaName, "jq_sys_notice", "popup_enabled",
                "ALTER TABLE jq_sys_notice ADD COLUMN popup_enabled TINYINT NOT NULL DEFAULT 0 AFTER scheduled_at");
        ensureColumnExists(schemaName, "jq_sys_notice", "valid_from",
                "ALTER TABLE jq_sys_notice ADD COLUMN valid_from DATETIME NULL AFTER popup_enabled");
        ensureColumnExists(schemaName, "jq_sys_notice", "valid_to",
                "ALTER TABLE jq_sys_notice ADD COLUMN valid_to DATETIME NULL AFTER valid_from");
        ensureColumnExists(schemaName, "jq_sys_notice", "target_type",
                "ALTER TABLE jq_sys_notice ADD COLUMN target_type VARCHAR(32) NOT NULL DEFAULT 'ALL' AFTER valid_to");
        ensureColumnExists(schemaName, "jq_sys_notice", "status",
                "ALTER TABLE jq_sys_notice ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' AFTER target_type");
        ensureColumnExists(schemaName, "jq_sys_notice", "published_at",
                "ALTER TABLE jq_sys_notice ADD COLUMN published_at DATETIME NULL AFTER status");
        ensureColumnExists(schemaName, "jq_sys_notice", "remark",
                "ALTER TABLE jq_sys_notice ADD COLUMN remark VARCHAR(255) NOT NULL DEFAULT '' AFTER published_at");
        ensureColumnExists(schemaName, "jq_sys_notice", "created_by",
                "ALTER TABLE jq_sys_notice ADD COLUMN created_by BIGINT NOT NULL DEFAULT 0 AFTER remark");
        ensureColumnExists(schemaName, "jq_sys_notice", "updated_by",
                "ALTER TABLE jq_sys_notice ADD COLUMN updated_by BIGINT NOT NULL DEFAULT 0 AFTER created_by");
        ensureColumnExists(schemaName, "jq_sys_notice", "created_at",
                "ALTER TABLE jq_sys_notice ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER updated_by");
        ensureColumnExists(schemaName, "jq_sys_notice", "updated_at",
                "ALTER TABLE jq_sys_notice ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at");
    }

    private void ensureNoticeTargetColumns(String schemaName) {
        ensureColumnExists(schemaName, "jq_sys_notice_target", "notice_id",
                "ALTER TABLE jq_sys_notice_target ADD COLUMN notice_id BIGINT NOT NULL DEFAULT 0 AFTER id");
        ensureColumnExists(schemaName, "jq_sys_notice_target", "target_type",
                "ALTER TABLE jq_sys_notice_target ADD COLUMN target_type VARCHAR(32) NOT NULL DEFAULT 'ALL' AFTER notice_id");
        ensureColumnExists(schemaName, "jq_sys_notice_target", "target_id",
                "ALTER TABLE jq_sys_notice_target ADD COLUMN target_id BIGINT NOT NULL DEFAULT 0 AFTER target_type");
        ensureColumnExists(schemaName, "jq_sys_notice_target", "created_at",
                "ALTER TABLE jq_sys_notice_target ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER target_id");
    }

    private void ensureNoticeUserColumns(String schemaName) {
        ensureColumnExists(schemaName, "jq_sys_notice_user", "notice_id",
                "ALTER TABLE jq_sys_notice_user ADD COLUMN notice_id BIGINT NOT NULL DEFAULT 0 AFTER id");
        ensureColumnExists(schemaName, "jq_sys_notice_user", "user_id",
                "ALTER TABLE jq_sys_notice_user ADD COLUMN user_id BIGINT NOT NULL DEFAULT 0 AFTER notice_id");
        ensureColumnExists(schemaName, "jq_sys_notice_user", "read_status",
                "ALTER TABLE jq_sys_notice_user ADD COLUMN read_status TINYINT NOT NULL DEFAULT 0 AFTER user_id");
        ensureColumnExists(schemaName, "jq_sys_notice_user", "read_at",
                "ALTER TABLE jq_sys_notice_user ADD COLUMN read_at DATETIME NULL AFTER read_status");
        ensureColumnExists(schemaName, "jq_sys_notice_user", "created_at",
                "ALTER TABLE jq_sys_notice_user ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER read_at");
        ensureColumnExists(schemaName, "jq_sys_notice_user", "updated_at",
                "ALTER TABLE jq_sys_notice_user ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at");
    }

    private void normalizeNoticeColumns() {
        jdbcTemplate.execute("ALTER TABLE jq_sys_notice MODIFY COLUMN title VARCHAR(128) NOT NULL");
        jdbcTemplate.execute("ALTER TABLE jq_sys_notice MODIFY COLUMN content TEXT NOT NULL");
        jdbcTemplate.execute("ALTER TABLE jq_sys_notice MODIFY COLUMN level VARCHAR(32) NOT NULL DEFAULT 'NORMAL'");
        jdbcTemplate.execute("ALTER TABLE jq_sys_notice MODIFY COLUMN publish_mode VARCHAR(32) NOT NULL DEFAULT 'IMMEDIATE'");
        jdbcTemplate.execute("ALTER TABLE jq_sys_notice MODIFY COLUMN popup_enabled TINYINT NOT NULL DEFAULT 0");
        jdbcTemplate.execute("ALTER TABLE jq_sys_notice MODIFY COLUMN target_type VARCHAR(32) NOT NULL DEFAULT 'ALL'");
        jdbcTemplate.execute("ALTER TABLE jq_sys_notice MODIFY COLUMN status VARCHAR(32) NOT NULL DEFAULT 'DRAFT'");
        jdbcTemplate.execute("ALTER TABLE jq_sys_notice MODIFY COLUMN remark VARCHAR(255) NOT NULL DEFAULT ''");
    }

    private void normalizeNoticeTargetColumns() {
        jdbcTemplate.execute("ALTER TABLE jq_sys_notice_target MODIFY COLUMN notice_id BIGINT NOT NULL");
        jdbcTemplate.execute("ALTER TABLE jq_sys_notice_target MODIFY COLUMN target_type VARCHAR(32) NOT NULL DEFAULT 'ALL'");
        jdbcTemplate.execute("ALTER TABLE jq_sys_notice_target MODIFY COLUMN target_id BIGINT NOT NULL DEFAULT 0");
    }

    private void normalizeNoticeUserColumns() {
        jdbcTemplate.execute("ALTER TABLE jq_sys_notice_user MODIFY COLUMN notice_id BIGINT NOT NULL");
        jdbcTemplate.execute("ALTER TABLE jq_sys_notice_user MODIFY COLUMN user_id BIGINT NOT NULL");
        jdbcTemplate.execute("ALTER TABLE jq_sys_notice_user MODIFY COLUMN read_status TINYINT NOT NULL DEFAULT 0");
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

package com.jianqing.module.system.startup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeSchemaInitializerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateNoticeTablesWhenMissing() throws Exception {
        NoticeSchemaInitializer initializer = new NoticeSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_notice")))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_notice_target")))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_notice_user")))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), anyString(), anyString()))
                .thenReturn(1);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate).execute("CREATE TABLE IF NOT EXISTS jq_sys_notice ("
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
                + "is_deleted TINYINT NOT NULL DEFAULT 0,"
                + "deleted_category VARCHAR(32) NULL,"
                + "deleted_at DATETIME NULL,"
                + "deleted_by BIGINT NULL,"
                + "created_by BIGINT NOT NULL DEFAULT 0,"
                + "updated_by BIGINT NOT NULL DEFAULT 0,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "KEY idx_jq_sys_notice_status_schedule (status, scheduled_at),"
                + "KEY idx_jq_sys_notice_published_at (published_at),"
                + "KEY idx_jq_sys_notice_deleted_category (is_deleted, deleted_category)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        verify(jdbcTemplate).execute("CREATE TABLE IF NOT EXISTS jq_sys_notice_target ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "notice_id BIGINT NOT NULL,"
                + "target_type VARCHAR(32) NOT NULL DEFAULT 'ALL',"
                + "target_id BIGINT NOT NULL DEFAULT 0,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "KEY idx_jq_sys_notice_target_notice_id (notice_id),"
                + "KEY idx_jq_sys_notice_target_type_id (target_type, target_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        verify(jdbcTemplate).execute("CREATE TABLE IF NOT EXISTS jq_sys_notice_user ("
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

    @Test
    void shouldRepairNoticeColumnsWhenTablesAlreadyExist() throws Exception {
        NoticeSchemaInitializer initializer = new NoticeSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"),
                eq(Integer.class), eq("jianqing"), anyString()))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_notice"), anyString()))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_notice_target"), anyString()))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_notice_user"), anyString()))
                .thenReturn(0);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate).execute("ALTER TABLE jq_sys_notice ADD COLUMN level VARCHAR(32) NOT NULL DEFAULT 'NORMAL' AFTER content");
        verify(jdbcTemplate).execute("ALTER TABLE jq_sys_notice ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 AFTER remark");
        verify(jdbcTemplate).execute("ALTER TABLE jq_sys_notice_target ADD COLUMN target_type VARCHAR(32) NOT NULL DEFAULT 'ALL' AFTER notice_id");
        verify(jdbcTemplate).execute("ALTER TABLE jq_sys_notice_user ADD COLUMN read_status TINYINT NOT NULL DEFAULT 0 AFTER user_id");
    }

    @Test
    void shouldSkipCreateWhenNoticeTablesReady() throws Exception {
        NoticeSchemaInitializer initializer = new NoticeSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"),
                eq(Integer.class), eq("jianqing"), anyString()))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), anyString(), anyString()))
                .thenReturn(1);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate, never()).execute("CREATE TABLE IF NOT EXISTS jq_sys_notice ("
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
                + "is_deleted TINYINT NOT NULL DEFAULT 0,"
                + "deleted_category VARCHAR(32) NULL,"
                + "deleted_at DATETIME NULL,"
                + "deleted_by BIGINT NULL,"
                + "created_by BIGINT NOT NULL DEFAULT 0,"
                + "updated_by BIGINT NOT NULL DEFAULT 0,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "KEY idx_jq_sys_notice_status_schedule (status, scheduled_at),"
                + "KEY idx_jq_sys_notice_published_at (published_at),"
                + "KEY idx_jq_sys_notice_deleted_category (is_deleted, deleted_category)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        verify(jdbcTemplate).execute("ALTER TABLE jq_sys_notice MODIFY COLUMN status VARCHAR(32) NOT NULL DEFAULT 'DRAFT'");
    }
}

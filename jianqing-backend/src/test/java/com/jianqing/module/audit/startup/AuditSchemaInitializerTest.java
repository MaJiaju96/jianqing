package com.jianqing.module.audit.startup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditSchemaInitializerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldRepairLegacyAuditSchema() throws Exception {
        AuditSchemaInitializer initializer = new AuditSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"), eq(Integer.class), eq("jianqing"), eq("jq_sys_oper_log")))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"), eq(Integer.class), eq("jianqing"), eq("jq_sys_login_log")))
                .thenReturn(0);
        mockOperColumnMissing("request_param");
        mockOperColumnMissing("response_data");
        mockOperColumnMissing("status");
        mockOperColumnMissing("error_msg");
        mockOperColumnMissing("cost_ms");
        mockOperColumnMissing("created_at");
        mockLoginColumnMissing("login_type");
        mockLoginColumnMissing("login_ip");
        mockLoginColumnMissing("login_location");
        mockLoginColumnMissing("user_agent");
        mockLoginColumnMissing("os");
        mockLoginColumnMissing("browser");
        mockLoginColumnMissing("status");
        mockLoginColumnMissing("msg");
        mockLoginColumnMissing("created_at");

        initializer.run(new DefaultApplicationArguments(new String[0]));

        InOrder inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).execute("CREATE TABLE IF NOT EXISTS jq_sys_oper_log (id BIGINT PRIMARY KEY AUTO_INCREMENT,trace_id VARCHAR(64) NOT NULL DEFAULT '',user_id BIGINT NOT NULL DEFAULT 0,username VARCHAR(64) NOT NULL DEFAULT '',module_name VARCHAR(64) NOT NULL DEFAULT '',biz_type VARCHAR(32) NOT NULL DEFAULT '',method VARCHAR(255) NOT NULL DEFAULT '',request_uri VARCHAR(255) NOT NULL DEFAULT '',request_ip VARCHAR(64) NOT NULL DEFAULT '',request_param LONGTEXT NULL,response_data LONGTEXT NULL,status TINYINT NOT NULL DEFAULT 1,error_msg VARCHAR(1000) NOT NULL DEFAULT '',cost_ms INT NOT NULL DEFAULT 0,created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,KEY idx_jq_sys_oper_log_user_id (user_id),KEY idx_jq_sys_oper_log_created_at (created_at),KEY idx_jq_sys_oper_log_status (status)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        inOrder.verify(jdbcTemplate).execute("CREATE TABLE IF NOT EXISTS jq_sys_login_log (id BIGINT PRIMARY KEY AUTO_INCREMENT,user_id BIGINT NOT NULL DEFAULT 0,username VARCHAR(64) NOT NULL DEFAULT '',login_type VARCHAR(32) NOT NULL DEFAULT 'password',login_ip VARCHAR(64) NOT NULL DEFAULT '',login_location VARCHAR(128) NOT NULL DEFAULT '',user_agent VARCHAR(512) NOT NULL DEFAULT '',os VARCHAR(64) NOT NULL DEFAULT '',browser VARCHAR(64) NOT NULL DEFAULT '',status TINYINT NOT NULL DEFAULT 1,msg VARCHAR(255) NOT NULL DEFAULT '',created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,KEY idx_jq_sys_login_log_user_id (user_id),KEY idx_jq_sys_login_log_created_at (created_at),KEY idx_jq_sys_login_log_status (status)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    @Test
    void shouldSkipRepairWhenAuditSchemaReady() throws Exception {
        AuditSchemaInitializer initializer = new AuditSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"), eq(Integer.class), eq("jianqing"), eq("jq_sys_oper_log")))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"), eq(Integer.class), eq("jianqing"), eq("jq_sys_login_log")))
                .thenReturn(1);
        mockOperColumnReady("request_param");
        mockOperColumnReady("response_data");
        mockOperColumnReady("status");
        mockOperColumnReady("error_msg");
        mockOperColumnReady("cost_ms");
        mockOperColumnReady("created_at");
        mockLoginColumnReady("login_type");
        mockLoginColumnReady("login_ip");
        mockLoginColumnReady("login_location");
        mockLoginColumnReady("user_agent");
        mockLoginColumnReady("os");
        mockLoginColumnReady("browser");
        mockLoginColumnReady("status");
        mockLoginColumnReady("msg");
        mockLoginColumnReady("created_at");

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate, never()).execute(eq("CREATE TABLE IF NOT EXISTS jq_sys_oper_log (id BIGINT PRIMARY KEY AUTO_INCREMENT,trace_id VARCHAR(64) NOT NULL DEFAULT '',user_id BIGINT NOT NULL DEFAULT 0,username VARCHAR(64) NOT NULL DEFAULT '',module_name VARCHAR(64) NOT NULL DEFAULT '',biz_type VARCHAR(32) NOT NULL DEFAULT '',method VARCHAR(255) NOT NULL DEFAULT '',request_uri VARCHAR(255) NOT NULL DEFAULT '',request_ip VARCHAR(64) NOT NULL DEFAULT '',request_param LONGTEXT NULL,response_data LONGTEXT NULL,status TINYINT NOT NULL DEFAULT 1,error_msg VARCHAR(1000) NOT NULL DEFAULT '',cost_ms INT NOT NULL DEFAULT 0,created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,KEY idx_jq_sys_oper_log_user_id (user_id),KEY idx_jq_sys_oper_log_created_at (created_at),KEY idx_jq_sys_oper_log_status (status)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"));
    }

    private void mockOperColumnMissing(String columnName) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_oper_log"), eq(columnName))).thenReturn(0);
    }

    private void mockOperColumnReady(String columnName) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_oper_log"), eq(columnName))).thenReturn(1);
    }

    private void mockLoginColumnMissing(String columnName) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_login_log"), eq(columnName))).thenReturn(0);
    }

    private void mockLoginColumnReady(String columnName) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_login_log"), eq(columnName))).thenReturn(1);
    }
}

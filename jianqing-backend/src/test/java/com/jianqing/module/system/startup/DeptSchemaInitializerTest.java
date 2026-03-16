package com.jianqing.module.system.startup;

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
class DeptSchemaInitializerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldRepairLegacyDeptSchema() throws Exception {
        DeptSchemaInitializer initializer = new DeptSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"), eq(Integer.class), eq("jianqing"), eq("jq_sys_dept")))
                .thenReturn(0);
        mockColumnMissing("leader_user_id");
        mockColumnMissing("phone");
        mockColumnMissing("email");
        mockColumnMissing("sort_no");
        mockColumnMissing("status");
        mockColumnMissing("is_deleted");
        mockColumnMissing("created_by");
        mockColumnMissing("updated_by");
        mockColumnMissing("created_at");
        mockColumnMissing("updated_at");

        initializer.run(new DefaultApplicationArguments(new String[0]));

        InOrder inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).execute("CREATE TABLE IF NOT EXISTS jq_sys_dept (id BIGINT PRIMARY KEY AUTO_INCREMENT,parent_id BIGINT NOT NULL DEFAULT 0,dept_name VARCHAR(64) NOT NULL,leader_user_id BIGINT NOT NULL DEFAULT 0,phone VARCHAR(20) NOT NULL DEFAULT '',email VARCHAR(128) NOT NULL DEFAULT '',sort_no INT NOT NULL DEFAULT 0,status TINYINT NOT NULL DEFAULT 1,is_deleted TINYINT NOT NULL DEFAULT 0,created_by BIGINT NOT NULL DEFAULT 0,updated_by BIGINT NOT NULL DEFAULT 0,created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,KEY idx_jq_sys_dept_parent_id (parent_id),KEY idx_jq_sys_dept_status (status)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    @Test
    void shouldSkipRepairWhenDeptSchemaReady() throws Exception {
        DeptSchemaInitializer initializer = new DeptSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"), eq(Integer.class), eq("jianqing"), eq("jq_sys_dept")))
                .thenReturn(1);
        mockColumnReady("leader_user_id");
        mockColumnReady("phone");
        mockColumnReady("email");
        mockColumnReady("sort_no");
        mockColumnReady("status");
        mockColumnReady("is_deleted");
        mockColumnReady("created_by");
        mockColumnReady("updated_by");
        mockColumnReady("created_at");
        mockColumnReady("updated_at");

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate, never()).execute(eq("CREATE TABLE IF NOT EXISTS jq_sys_dept (id BIGINT PRIMARY KEY AUTO_INCREMENT,parent_id BIGINT NOT NULL DEFAULT 0,dept_name VARCHAR(64) NOT NULL,leader_user_id BIGINT NOT NULL DEFAULT 0,phone VARCHAR(20) NOT NULL DEFAULT '',email VARCHAR(128) NOT NULL DEFAULT '',sort_no INT NOT NULL DEFAULT 0,status TINYINT NOT NULL DEFAULT 1,is_deleted TINYINT NOT NULL DEFAULT 0,created_by BIGINT NOT NULL DEFAULT 0,updated_by BIGINT NOT NULL DEFAULT 0,created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,KEY idx_jq_sys_dept_parent_id (parent_id),KEY idx_jq_sys_dept_status (status)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"));
    }

    private void mockColumnMissing(String columnName) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_dept"), eq(columnName))).thenReturn(0);
    }

    private void mockColumnReady(String columnName) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_dept"), eq(columnName))).thenReturn(1);
    }
}

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
class UserRoleSchemaInitializerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldRepairLegacyUserRoleSchema() throws Exception {
        UserRoleSchemaInitializer initializer = new UserRoleSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        mockTable("jq_sys_user", 0);
        mockTable("jq_sys_role", 0);
        mockTable("jq_sys_user_role", 0);
        mockTable("jq_sys_role_dept", 0);
        mockUserColumnMissing("mobile");
        mockUserColumnMissing("email");
        mockUserColumnMissing("dept_id");
        mockUserColumnMissing("status");
        mockUserColumnMissing("is_deleted");
        mockUserColumnMissing("last_login_ip");
        mockUserColumnMissing("last_login_time");
        mockUserColumnMissing("remark");
        mockUserColumnMissing("created_by");
        mockUserColumnMissing("updated_by");
        mockUserColumnMissing("created_at");
        mockUserColumnMissing("updated_at");
        mockRoleColumnMissing("data_scope");
        mockRoleColumnMissing("status");
        mockRoleColumnMissing("is_deleted");
        mockRoleColumnMissing("remark");
        mockRoleColumnMissing("created_by");
        mockRoleColumnMissing("updated_by");
        mockRoleColumnMissing("created_at");
        mockRoleColumnMissing("updated_at");

        initializer.run(new DefaultApplicationArguments(new String[0]));

        InOrder inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).execute("CREATE TABLE IF NOT EXISTS jq_sys_user (id BIGINT PRIMARY KEY AUTO_INCREMENT,username VARCHAR(64) NOT NULL,password_hash VARCHAR(255) NOT NULL,nickname VARCHAR(64) NOT NULL DEFAULT '',real_name VARCHAR(64) NOT NULL DEFAULT '',mobile VARCHAR(20) NOT NULL DEFAULT '',email VARCHAR(128) NOT NULL DEFAULT '',dept_id BIGINT NOT NULL DEFAULT 0,status TINYINT NOT NULL DEFAULT 1,is_deleted TINYINT NOT NULL DEFAULT 0,last_login_ip VARCHAR(64) NOT NULL DEFAULT '',last_login_time DATETIME NULL,remark VARCHAR(255) NOT NULL DEFAULT '',created_by BIGINT NOT NULL DEFAULT 0,updated_by BIGINT NOT NULL DEFAULT 0,created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,UNIQUE KEY uk_jq_sys_user_username (username),KEY idx_jq_sys_user_dept_id (dept_id),KEY idx_jq_sys_user_status (status)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        inOrder.verify(jdbcTemplate).execute("CREATE TABLE IF NOT EXISTS jq_sys_role (id BIGINT PRIMARY KEY AUTO_INCREMENT,role_name VARCHAR(64) NOT NULL,role_code VARCHAR(64) NOT NULL,data_scope TINYINT NOT NULL DEFAULT 1,status TINYINT NOT NULL DEFAULT 1,is_deleted TINYINT NOT NULL DEFAULT 0,remark VARCHAR(255) NOT NULL DEFAULT '',created_by BIGINT NOT NULL DEFAULT 0,updated_by BIGINT NOT NULL DEFAULT 0,created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,UNIQUE KEY uk_jq_sys_role_role_code (role_code),KEY idx_jq_sys_role_status (status)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        inOrder.verify(jdbcTemplate).execute("CREATE TABLE IF NOT EXISTS jq_sys_user_role (id BIGINT PRIMARY KEY AUTO_INCREMENT,user_id BIGINT NOT NULL,role_id BIGINT NOT NULL,created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE KEY uk_jq_sys_user_role (user_id, role_id),KEY idx_jq_sys_user_role_role_id (role_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        inOrder.verify(jdbcTemplate).execute("CREATE TABLE IF NOT EXISTS jq_sys_role_dept (id BIGINT PRIMARY KEY AUTO_INCREMENT,role_id BIGINT NOT NULL,dept_id BIGINT NOT NULL,created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE KEY uk_jq_sys_role_dept (role_id, dept_id),KEY idx_jq_sys_role_dept_dept_id (dept_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    @Test
    void shouldSkipRepairWhenUserRoleSchemaReady() throws Exception {
        UserRoleSchemaInitializer initializer = new UserRoleSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        mockTable("jq_sys_user", 1);
        mockTable("jq_sys_role", 1);
        mockTable("jq_sys_user_role", 1);
        mockTable("jq_sys_role_dept", 1);
        mockUserColumnReady("mobile");
        mockUserColumnReady("email");
        mockUserColumnReady("dept_id");
        mockUserColumnReady("status");
        mockUserColumnReady("is_deleted");
        mockUserColumnReady("last_login_ip");
        mockUserColumnReady("last_login_time");
        mockUserColumnReady("remark");
        mockUserColumnReady("created_by");
        mockUserColumnReady("updated_by");
        mockUserColumnReady("created_at");
        mockUserColumnReady("updated_at");
        mockRoleColumnReady("data_scope");
        mockRoleColumnReady("status");
        mockRoleColumnReady("is_deleted");
        mockRoleColumnReady("remark");
        mockRoleColumnReady("created_by");
        mockRoleColumnReady("updated_by");
        mockRoleColumnReady("created_at");
        mockRoleColumnReady("updated_at");

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate, never()).execute(eq("CREATE TABLE IF NOT EXISTS jq_sys_user (id BIGINT PRIMARY KEY AUTO_INCREMENT,username VARCHAR(64) NOT NULL,password_hash VARCHAR(255) NOT NULL,nickname VARCHAR(64) NOT NULL DEFAULT '',real_name VARCHAR(64) NOT NULL DEFAULT '',mobile VARCHAR(20) NOT NULL DEFAULT '',email VARCHAR(128) NOT NULL DEFAULT '',dept_id BIGINT NOT NULL DEFAULT 0,status TINYINT NOT NULL DEFAULT 1,is_deleted TINYINT NOT NULL DEFAULT 0,last_login_ip VARCHAR(64) NOT NULL DEFAULT '',last_login_time DATETIME NULL,remark VARCHAR(255) NOT NULL DEFAULT '',created_by BIGINT NOT NULL DEFAULT 0,updated_by BIGINT NOT NULL DEFAULT 0,created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,UNIQUE KEY uk_jq_sys_user_username (username),KEY idx_jq_sys_user_dept_id (dept_id),KEY idx_jq_sys_user_status (status)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"));
    }

    private void mockTable(String tableName, int count) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"),
                eq(Integer.class), eq("jianqing"), eq(tableName))).thenReturn(count);
    }

    private void mockUserColumnMissing(String columnName) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_user"), eq(columnName))).thenReturn(0);
    }

    private void mockUserColumnReady(String columnName) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_user"), eq(columnName))).thenReturn(1);
    }

    private void mockRoleColumnMissing(String columnName) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_role"), eq(columnName))).thenReturn(0);
    }

    private void mockRoleColumnReady(String columnName) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_role"), eq(columnName))).thenReturn(1);
    }
}

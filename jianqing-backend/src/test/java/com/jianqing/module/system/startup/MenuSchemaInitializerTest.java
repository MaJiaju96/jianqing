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
class MenuSchemaInitializerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldRepairLegacyMenuSchema() throws Exception {
        MenuSchemaInitializer initializer = new MenuSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("route_path"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("component"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("perms"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("icon"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("sort_no"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("visible"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("status"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("is_deleted"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("created_by"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("updated_by"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("created_at"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("updated_at"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_role_menu"))).thenReturn(0);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        InOrder inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_menu ADD COLUMN route_path VARCHAR(128) NOT NULL DEFAULT '' AFTER menu_name");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_menu ADD COLUMN component VARCHAR(255) NOT NULL DEFAULT '' AFTER route_path");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_menu ADD COLUMN perms VARCHAR(128) NOT NULL DEFAULT '' AFTER component");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_menu ADD COLUMN icon VARCHAR(64) NOT NULL DEFAULT '' AFTER perms");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_menu ADD COLUMN sort_no INT NOT NULL DEFAULT 0 AFTER icon");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_menu ADD COLUMN visible TINYINT NOT NULL DEFAULT 1 AFTER sort_no");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_menu ADD COLUMN status TINYINT NOT NULL DEFAULT 1 AFTER visible");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_menu ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 AFTER status");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_menu ADD COLUMN created_by BIGINT NOT NULL DEFAULT 0 AFTER is_deleted");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_menu ADD COLUMN updated_by BIGINT NOT NULL DEFAULT 0 AFTER created_by");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_menu ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER updated_by");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_menu ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at");
        inOrder.verify(jdbcTemplate).execute("CREATE TABLE IF NOT EXISTS jq_sys_role_menu (id BIGINT PRIMARY KEY AUTO_INCREMENT,role_id BIGINT NOT NULL,menu_id BIGINT NOT NULL,created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE KEY uk_jq_sys_role_menu (role_id, menu_id),KEY idx_jq_sys_role_menu_menu_id (menu_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    @Test
    void shouldSkipRepairWhenMenuSchemaReady() throws Exception {
        MenuSchemaInitializer initializer = new MenuSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("route_path"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("component"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("perms"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("icon"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("sort_no"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("visible"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("status"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("is_deleted"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("created_by"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("updated_by"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("created_at"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_menu"), eq("updated_at"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_role_menu"))).thenReturn(1);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate, never()).execute(eq("ALTER TABLE jq_sys_menu ADD COLUMN route_path VARCHAR(128) NOT NULL DEFAULT '' AFTER menu_name"));
    }
}

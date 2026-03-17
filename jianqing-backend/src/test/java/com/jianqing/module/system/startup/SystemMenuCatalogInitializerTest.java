package com.jianqing.module.system.startup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemMenuCatalogInitializerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateSettingsRootAndMoveConfigAndDictMenus() throws Exception {
        SystemMenuCatalogInitializer initializer = new SystemMenuCatalogInitializer(jdbcTemplate);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE route_path = ? AND menu_type = 1 AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("/settings")))
                .thenReturn(null)
                .thenReturn(30L);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM jq_sys_role_menu WHERE role_id = ? AND menu_id = ?"), eq(Integer.class), eq(1L), eq(30L)))
                .thenReturn(0);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("system:config:list")))
                .thenReturn(7L);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("system:dict:list")))
                .thenReturn(6L);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        InOrder inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).update(eq("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                eq(0L), eq(1), eq("参数管理"), eq("/settings"), eq("Layout"), eq(""), eq("Tools"), eq(15), eq(1), eq(1), eq(0), eq(1L), eq(1L));
        verify(jdbcTemplate).update("INSERT INTO jq_sys_role_menu (role_id, menu_id) VALUES (?, ?)", 1L, 30L);
        verify(jdbcTemplate).update("UPDATE jq_sys_menu SET parent_id = ?, sort_no = ? WHERE id = ?", 30L, 1, 7L);
        verify(jdbcTemplate).update("UPDATE jq_sys_menu SET parent_id = ?, sort_no = ? WHERE id = ?", 30L, 2, 6L);
    }

    @Test
    void shouldReuseSettingsRootWhenAlreadyExists() throws Exception {
        SystemMenuCatalogInitializer initializer = new SystemMenuCatalogInitializer(jdbcTemplate);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE route_path = ? AND menu_type = 1 AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("/settings")))
                .thenReturn(30L);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM jq_sys_role_menu WHERE role_id = ? AND menu_id = ?"), eq(Integer.class), eq(1L), eq(30L)))
                .thenReturn(1);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("system:config:list")))
                .thenReturn(null);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("system:dict:list")))
                .thenReturn(null);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate, never()).update(eq("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(jdbcTemplate, never()).update("INSERT INTO jq_sys_role_menu (role_id, menu_id) VALUES (?, ?)", 1L, 30L);
    }
}

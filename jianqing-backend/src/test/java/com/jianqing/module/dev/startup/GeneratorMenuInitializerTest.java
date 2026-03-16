package com.jianqing.module.dev.startup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneratorMenuInitializerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateGeneratorMenusAndBindAdminRole() throws Exception {
        GeneratorMenuInitializer initializer = new GeneratorMenuInitializer(jdbcTemplate);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("system:generator:list")))
                .thenReturn(null)
                .thenReturn(10L);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("system:generator:query")))
                .thenReturn(null)
                .thenReturn(11L);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM jq_sys_role_menu WHERE role_id = ? AND menu_id = ?"), eq(Integer.class), eq(1L), eq(10L)))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM jq_sys_role_menu WHERE role_id = ? AND menu_id = ?"), eq(Integer.class), eq(1L), eq(11L)))
                .thenReturn(0);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        InOrder inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).update(eq("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                eq(1L), eq(2), eq("代码生成"), eq("generator"), eq("system/generator/index"), eq("system:generator:list"),
                eq("MagicStick"), eq(7), eq(1), eq(1), eq(0), eq(1L), eq(1L));
        inOrder.verify(jdbcTemplate).update(eq("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                eq(10L), eq(3), eq("代码生成查询"), eq(""), eq(""), eq("system:generator:query"),
                eq(""), eq(1), eq(1), eq(1), eq(0), eq(1L), eq(1L));
        verify(jdbcTemplate).update("INSERT INTO jq_sys_role_menu (role_id, menu_id) VALUES (?, ?)", 1L, 10L);
        verify(jdbcTemplate).update("INSERT INTO jq_sys_role_menu (role_id, menu_id) VALUES (?, ?)", 1L, 11L);
    }

    @Test
    void shouldSkipMenuCreationWhenAlreadyExists() throws Exception {
        GeneratorMenuInitializer initializer = new GeneratorMenuInitializer(jdbcTemplate);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("system:generator:list")))
                .thenReturn(10L);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("system:generator:query")))
                .thenReturn(11L);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM jq_sys_role_menu WHERE role_id = ? AND menu_id = ?"), eq(Integer.class), eq(1L), eq(10L)))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM jq_sys_role_menu WHERE role_id = ? AND menu_id = ?"), eq(Integer.class), eq(1L), eq(11L)))
                .thenReturn(1);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate, never()).update(eq("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(jdbcTemplate, never()).update("INSERT INTO jq_sys_role_menu (role_id, menu_id) VALUES (?, ?)", 1L, 10L);
    }
}

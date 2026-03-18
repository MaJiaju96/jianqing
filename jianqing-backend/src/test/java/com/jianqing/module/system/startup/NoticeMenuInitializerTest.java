package com.jianqing.module.system.startup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeMenuInitializerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateNoticeMenusAndButtons() throws Exception {
        NoticeMenuInitializer initializer = new NoticeMenuInitializer(jdbcTemplate);
        mockRootMenu();
        mockMyNoticeMenu();
        mockNoticeManageMenu();
        mockButtonMenu("system:notice:add", 33L);
        mockButtonMenu("system:notice:edit", 34L);
        mockButtonMenu("system:notice:publish", 35L);
        mockButtonMenu("system:notice:cancel", 36L);
        mockButtonMenu("system:notice:delete", 37L);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate).update(eq("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                eq(0L), eq(1), eq("消息中心"), eq("/messages"), eq("Layout"), eq(""), eq("Bell"), eq(19), eq(1), eq(1), eq(0), eq(1L), eq(1L));
        verify(jdbcTemplate).update(eq("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                eq(30L), eq(2), eq("我的消息"), eq("mine"), eq("messages/mine/index"), eq(""), eq("Message"), eq(1), eq(1), eq(1), eq(0), eq(1L), eq(1L));
        verify(jdbcTemplate).update(eq("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                eq(30L), eq(2), eq("通知管理"), eq("manage"), eq("messages/manage/index"), eq("system:notice:list"), eq("BellFilled"), eq(2), eq(1), eq(1), eq(0), eq(1L), eq(1L));
        verify(jdbcTemplate).update("UPDATE jq_sys_menu SET parent_id = ?, sort_no = ? WHERE id = ?", 30L, 2, 32L);
        verify(jdbcTemplate).update("UPDATE jq_sys_menu SET parent_id = ?, sort_no = ? WHERE id = ?", 32L, 1, 33L);
        verify(jdbcTemplate).update("UPDATE jq_sys_menu SET parent_id = ?, sort_no = ? WHERE id = ?", 32L, 5, 37L);
        verify(jdbcTemplate).update("INSERT INTO jq_sys_role_menu (role_id, menu_id) VALUES (?, ?)", 1L, 30L);
        verify(jdbcTemplate).update("INSERT INTO jq_sys_role_menu (role_id, menu_id) VALUES (?, ?)", 1L, 31L);
        verify(jdbcTemplate).update("INSERT INTO jq_sys_role_menu (role_id, menu_id) VALUES (?, ?)", 1L, 32L);
        verify(jdbcTemplate).update("INSERT INTO jq_sys_role_menu (role_id, menu_id) VALUES (?, ?)", 1L, 33L);
        verify(jdbcTemplate).update("INSERT INTO jq_sys_role_menu (role_id, menu_id) VALUES (?, ?)", 1L, 37L);
    }

    @Test
    void shouldReuseExistingNoticeMenus() throws Exception {
        NoticeMenuInitializer initializer = new NoticeMenuInitializer(jdbcTemplate);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE route_path = ? AND menu_type = 1 AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("/messages")))
                .thenReturn(30L);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE parent_id = ? AND route_path = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq(30L), eq("mine")))
                .thenReturn(31L);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("system:notice:list")))
                .thenReturn(32L);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("system:notice:add")))
                .thenReturn(33L);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("system:notice:edit")))
                .thenReturn(34L);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("system:notice:publish")))
                .thenReturn(35L);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("system:notice:cancel")))
                .thenReturn(36L);
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("system:notice:delete")))
                .thenReturn(37L);
        mockRoleBindingCount(30L, 1);
        mockRoleBindingCount(31L, 1);
        mockRoleBindingCount(32L, 1);
        mockRoleBindingCount(33L, 1);
        mockRoleBindingCount(34L, 1);
        mockRoleBindingCount(35L, 1);
        mockRoleBindingCount(36L, 1);
        mockRoleBindingCount(37L, 1);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate, never()).update(eq("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(jdbcTemplate, never()).update("INSERT INTO jq_sys_role_menu (role_id, menu_id) VALUES (?, ?)", 1L, 32L);
    }

    private void mockRootMenu() {
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE route_path = ? AND menu_type = 1 AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("/messages")))
                .thenReturn(null)
                .thenReturn(30L);
        mockRoleBindingCount(30L, 0);
    }

    private void mockMyNoticeMenu() {
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE parent_id = ? AND route_path = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq(30L), eq("mine")))
                .thenReturn(null)
                .thenReturn(31L);
        mockRoleBindingCount(31L, 0);
    }

    private void mockNoticeManageMenu() {
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq("system:notice:list")))
                .thenReturn(null)
                .thenReturn(32L);
        mockRoleBindingCount(32L, 0);
    }

    private void mockButtonMenu(String perms, Long menuId) {
        when(jdbcTemplate.query(eq("SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1"), any(ResultSetExtractor.class), eq(perms)))
                .thenReturn(null)
                .thenReturn(menuId);
        mockRoleBindingCount(menuId, 0);
    }

    private void mockRoleBindingCount(Long menuId, Integer count) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM jq_sys_role_menu WHERE role_id = ? AND menu_id = ?"), eq(Integer.class), eq(1L), eq(menuId)))
                .thenReturn(count);
    }
}

package com.jianqing.module.system.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class NoticeMenuInitializer implements ApplicationRunner {

    private static final String MESSAGE_ROOT_ROUTE = "/messages";
    private static final String MY_NOTICE_ROUTE = "mine";
    private static final String NOTICE_LIST_PERMS = "system:notice:list";
    private static final String NOTICE_ADD_PERMS = "system:notice:add";
    private static final String NOTICE_EDIT_PERMS = "system:notice:edit";
    private static final String NOTICE_PUBLISH_PERMS = "system:notice:publish";
    private static final String NOTICE_CANCEL_PERMS = "system:notice:cancel";
    private static final String NOTICE_DELETE_PERMS = "system:notice:delete";

    private final JdbcTemplate jdbcTemplate;

    public NoticeMenuInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long rootId = ensureMessageRootMenu();
        if (rootId == null) {
            return;
        }
        bindAdminRole(rootId);
        Long myNoticeMenuId = ensureMyNoticeMenu(rootId);
        Long noticeManageMenuId = ensureNoticeManageMenu(rootId);
        if (myNoticeMenuId != null) {
            bindAdminRole(myNoticeMenuId);
        }
        if (noticeManageMenuId == null) {
            return;
        }
        bindAdminRole(noticeManageMenuId);
        ensureButtonMenu(noticeManageMenuId, "通知新增", NOTICE_ADD_PERMS, 1);
        ensureButtonMenu(noticeManageMenuId, "通知修改", NOTICE_EDIT_PERMS, 2);
        ensureButtonMenu(noticeManageMenuId, "通知发布", NOTICE_PUBLISH_PERMS, 3);
        ensureButtonMenu(noticeManageMenuId, "通知取消", NOTICE_CANCEL_PERMS, 4);
        ensureButtonMenu(noticeManageMenuId, "通知删除", NOTICE_DELETE_PERMS, 5);
    }

    private Long ensureMessageRootMenu() {
        Long menuId = findRootMenuIdByRoutePath(MESSAGE_ROOT_ROUTE);
        if (menuId != null) {
            return menuId;
        }
        jdbcTemplate.update("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                0L, 1, "消息中心", MESSAGE_ROOT_ROUTE, "Layout", "", "Bell", 19, 1, 1, 0, 1L, 1L);
        return findRootMenuIdByRoutePath(MESSAGE_ROOT_ROUTE);
    }

    private Long ensureMyNoticeMenu(Long parentId) {
        Long menuId = findMenuIdByParentAndRoutePath(parentId, MY_NOTICE_ROUTE);
        if (menuId != null) {
            moveMenu(parentId, menuId, 1);
            return menuId;
        }
        jdbcTemplate.update("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                parentId, 2, "我的消息", MY_NOTICE_ROUTE, "messages/mine/index", "", "Message", 1, 1, 1, 0, 1L, 1L);
        return findMenuIdByParentAndRoutePath(parentId, MY_NOTICE_ROUTE);
    }

    private Long ensureNoticeManageMenu(Long parentId) {
        Long menuId = findMenuIdByPerms(NOTICE_LIST_PERMS);
        if (menuId == null) {
            jdbcTemplate.update("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    parentId, 2, "通知管理", "manage", "messages/manage/index", NOTICE_LIST_PERMS, "BellFilled", 2, 1, 1, 0, 1L, 1L);
            menuId = findMenuIdByPerms(NOTICE_LIST_PERMS);
        }
        if (menuId != null) {
            moveMenu(parentId, menuId, 2);
        }
        return menuId;
    }

    private void ensureButtonMenu(Long parentId, String menuName, String perms, int sortNo) {
        Long menuId = findMenuIdByPerms(perms);
        if (menuId == null) {
            jdbcTemplate.update("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    parentId, 3, menuName, "", "", perms, "", sortNo, 1, 1, 0, 1L, 1L);
            menuId = findMenuIdByPerms(perms);
        }
        if (menuId != null) {
            moveMenu(parentId, menuId, sortNo);
            bindAdminRole(menuId);
        }
    }

    private void moveMenu(Long parentId, Long menuId, int sortNo) {
        jdbcTemplate.update("UPDATE jq_sys_menu SET parent_id = ?, sort_no = ? WHERE id = ?", parentId, sortNo, menuId);
    }

    private void bindAdminRole(Long menuId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM jq_sys_role_menu WHERE role_id = ? AND menu_id = ?",
                Integer.class, 1L, menuId);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update("INSERT INTO jq_sys_role_menu (role_id, menu_id) VALUES (?, ?)", 1L, menuId);
    }

    private Long findRootMenuIdByRoutePath(String routePath) {
        return jdbcTemplate.query(
                "SELECT id FROM jq_sys_menu WHERE route_path = ? AND menu_type = 1 AND is_deleted = 0 ORDER BY id ASC LIMIT 1",
                resultSet -> resultSet.next() ? resultSet.getLong(1) : null,
                routePath);
    }

    private Long findMenuIdByParentAndRoutePath(Long parentId, String routePath) {
        return jdbcTemplate.query(
                "SELECT id FROM jq_sys_menu WHERE parent_id = ? AND route_path = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1",
                resultSet -> resultSet.next() ? resultSet.getLong(1) : null,
                parentId, routePath);
    }

    private Long findMenuIdByPerms(String perms) {
        return jdbcTemplate.query(
                "SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1",
                resultSet -> resultSet.next() ? resultSet.getLong(1) : null,
                perms);
    }
}

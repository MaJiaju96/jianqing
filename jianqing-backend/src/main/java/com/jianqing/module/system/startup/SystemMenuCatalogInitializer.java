package com.jianqing.module.system.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SystemMenuCatalogInitializer implements ApplicationRunner {

    private static final String SETTINGS_ROOT_ROUTE = "/settings";
    private static final String CONFIG_LIST_PERMS = "system:config:list";
    private static final String DICT_LIST_PERMS = "system:dict:list";

    private final JdbcTemplate jdbcTemplate;

    public SystemMenuCatalogInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long settingsRootId = ensureSettingsRootMenu();
        if (settingsRootId == null) {
            return;
        }
        bindAdminRole(settingsRootId);
        moveMenuToRoot(CONFIG_LIST_PERMS, settingsRootId, 1);
        moveMenuToRoot(DICT_LIST_PERMS, settingsRootId, 2);
    }

    private Long ensureSettingsRootMenu() {
        Long menuId = findMenuIdByRoutePath(SETTINGS_ROOT_ROUTE);
        if (menuId != null) {
            return menuId;
        }
        jdbcTemplate.update("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                0L, 1, "参数管理", SETTINGS_ROOT_ROUTE, "Layout", "", "Tools", 15, 1, 1, 0, 1L, 1L);
        return findMenuIdByRoutePath(SETTINGS_ROOT_ROUTE);
    }

    private void moveMenuToRoot(String perms, Long parentId, int sortNo) {
        Long menuId = findMenuIdByPerms(perms);
        if (menuId == null) {
            return;
        }
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

    private Long findMenuIdByRoutePath(String routePath) {
        return jdbcTemplate.query(
                "SELECT id FROM jq_sys_menu WHERE route_path = ? AND menu_type = 1 AND is_deleted = 0 ORDER BY id ASC LIMIT 1",
                resultSet -> resultSet.next() ? resultSet.getLong(1) : null,
                routePath);
    }

    private Long findMenuIdByPerms(String perms) {
        return jdbcTemplate.query(
                "SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1",
                resultSet -> resultSet.next() ? resultSet.getLong(1) : null,
                perms);
    }
}

package com.jianqing.module.dev.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class GeneratorMenuInitializer implements ApplicationRunner {

    private static final String GENERATOR_LIST_PERMS = "system:generator:list";
    private static final String GENERATOR_QUERY_PERMS = "system:generator:query";

    private final JdbcTemplate jdbcTemplate;

    public GeneratorMenuInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long generatorMenuId = ensureGeneratorListMenu();
        if (generatorMenuId == null) {
            return;
        }
        Long generatorQueryMenuId = ensureGeneratorQueryMenu(generatorMenuId);
        bindAdminRole(generatorMenuId);
        if (generatorQueryMenuId != null) {
            bindAdminRole(generatorQueryMenuId);
        }
    }

    private Long ensureGeneratorListMenu() {
        Long menuId = findMenuIdByPerms(GENERATOR_LIST_PERMS);
        if (menuId != null) {
            return menuId;
        }
        jdbcTemplate.update("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                1L, 2, "代码生成", "generator", "system/generator/index", GENERATOR_LIST_PERMS,
                "MagicStick", 7, 1, 1, 0, 1L, 1L);
        return findMenuIdByPerms(GENERATOR_LIST_PERMS);
    }

    private Long ensureGeneratorQueryMenu(Long parentId) {
        Long menuId = findMenuIdByPerms(GENERATOR_QUERY_PERMS);
        if (menuId != null) {
            return menuId;
        }
        jdbcTemplate.update("INSERT INTO jq_sys_menu (parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                parentId, 3, "代码生成查询", "", "", GENERATOR_QUERY_PERMS,
                "", 1, 1, 1, 0, 1L, 1L);
        return findMenuIdByPerms(GENERATOR_QUERY_PERMS);
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

    private Long findMenuIdByPerms(String perms) {
        return jdbcTemplate.query(
                "SELECT id FROM jq_sys_menu WHERE perms = ? AND is_deleted = 0 ORDER BY id ASC LIMIT 1",
                resultSet -> resultSet.next() ? resultSet.getLong(1) : null,
                perms);
    }
}

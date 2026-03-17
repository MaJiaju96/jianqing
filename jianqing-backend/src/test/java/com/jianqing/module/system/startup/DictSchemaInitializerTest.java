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
class DictSchemaInitializerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldRepairLegacyDictSchema() throws Exception {
        DictSchemaInitializer initializer = new DictSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"), eq(Integer.class), eq("jianqing"), eq("jq_sys_dict_type")))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"), eq(Integer.class), eq("jianqing"), eq("jq_sys_dict_data")))
                .thenReturn(0);
        mockColumnMissing("jq_sys_dict_type", "status");
        mockColumnMissing("jq_sys_dict_type", "remark");
        mockColumnMissing("jq_sys_dict_type", "created_by");
        mockColumnMissing("jq_sys_dict_type", "updated_by");
        mockColumnMissing("jq_sys_dict_type", "created_at");
        mockColumnMissing("jq_sys_dict_type", "updated_at");
        mockColumnMissing("jq_sys_dict_data", "color_type");
        mockColumnMissing("jq_sys_dict_data", "css_class");
        mockColumnMissing("jq_sys_dict_data", "sort_no");
        mockColumnMissing("jq_sys_dict_data", "status");
        mockColumnMissing("jq_sys_dict_data", "remark");
        mockColumnMissing("jq_sys_dict_data", "created_by");
        mockColumnMissing("jq_sys_dict_data", "updated_by");
        mockColumnMissing("jq_sys_dict_data", "created_at");
        mockColumnMissing("jq_sys_dict_data", "updated_at");

        mockDictTypeMissing("sys_common_status");
        mockDictTypeMissing("sys_dept_status");
        mockDictDataMissing("sys_common_status", "1");
        mockDictDataMissing("sys_common_status", "0");
        mockDictDataMissing("sys_dept_status", "1");
        mockDictDataMissing("sys_dept_status", "0");

        initializer.run(new DefaultApplicationArguments(new String[0]));

        InOrder inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).execute("CREATE TABLE IF NOT EXISTS jq_sys_dict_type (id BIGINT PRIMARY KEY AUTO_INCREMENT,dict_name VARCHAR(64) NOT NULL,dict_type VARCHAR(64) NOT NULL,status TINYINT NOT NULL DEFAULT 1,remark VARCHAR(255) NOT NULL DEFAULT '',created_by BIGINT NOT NULL DEFAULT 0,updated_by BIGINT NOT NULL DEFAULT 0,created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,UNIQUE KEY uk_jq_sys_dict_type_type (dict_type)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        inOrder.verify(jdbcTemplate).execute("CREATE TABLE IF NOT EXISTS jq_sys_dict_data (id BIGINT PRIMARY KEY AUTO_INCREMENT,dict_type VARCHAR(64) NOT NULL,label VARCHAR(64) NOT NULL,value VARCHAR(64) NOT NULL,color_type VARCHAR(32) NOT NULL DEFAULT '',css_class VARCHAR(64) NOT NULL DEFAULT '',sort_no INT NOT NULL DEFAULT 0,status TINYINT NOT NULL DEFAULT 1,remark VARCHAR(255) NOT NULL DEFAULT '',created_by BIGINT NOT NULL DEFAULT 0,updated_by BIGINT NOT NULL DEFAULT 0,created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,UNIQUE KEY uk_jq_sys_dict_data_type_value (dict_type, value),KEY idx_jq_sys_dict_data_type (dict_type)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        inOrder.verify(jdbcTemplate).update("INSERT INTO jq_sys_dict_type (dict_name, dict_type, status, remark, created_by, updated_by) VALUES (?, ?, 1, ?, 1, 1)", "通用状态", "sys_common_status", "用户/角色/菜单通用启禁用状态");
        inOrder.verify(jdbcTemplate).update("INSERT INTO jq_sys_dict_type (dict_name, dict_type, status, remark, created_by, updated_by) VALUES (?, ?, 1, ?, 1, 1)", "部门状态", "sys_dept_status", "部门启用/禁用状态");
        inOrder.verify(jdbcTemplate).update("INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by) VALUES (?, ?, ?, ?, '', ?, 1, ?, 1, 1)", "sys_common_status", "启用", "1", "success", 1, "启用");
        inOrder.verify(jdbcTemplate).update("INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by) VALUES (?, ?, ?, ?, '', ?, 1, ?, 1, 1)", "sys_common_status", "禁用", "0", "danger", 2, "禁用");
        inOrder.verify(jdbcTemplate).update("INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by) VALUES (?, ?, ?, ?, '', ?, 1, ?, 1, 1)", "sys_dept_status", "启用", "1", "success", 1, "部门启用");
        inOrder.verify(jdbcTemplate).update("INSERT INTO jq_sys_dict_data (dict_type, label, value, color_type, css_class, sort_no, status, remark, created_by, updated_by) VALUES (?, ?, ?, ?, '', ?, 1, ?, 1, 1)", "sys_dept_status", "禁用", "0", "info", 2, "部门禁用");
    }

    @Test
    void shouldSkipRepairWhenDictSchemaReady() throws Exception {
        DictSchemaInitializer initializer = new DictSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"), eq(Integer.class), eq("jianqing"), eq("jq_sys_dict_type")))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"), eq(Integer.class), eq("jianqing"), eq("jq_sys_dict_data")))
                .thenReturn(1);
        mockColumnReady("jq_sys_dict_type", "status");
        mockColumnReady("jq_sys_dict_type", "remark");
        mockColumnReady("jq_sys_dict_type", "created_by");
        mockColumnReady("jq_sys_dict_type", "updated_by");
        mockColumnReady("jq_sys_dict_type", "created_at");
        mockColumnReady("jq_sys_dict_type", "updated_at");
        mockColumnReady("jq_sys_dict_data", "color_type");
        mockColumnReady("jq_sys_dict_data", "css_class");
        mockColumnReady("jq_sys_dict_data", "sort_no");
        mockColumnReady("jq_sys_dict_data", "status");
        mockColumnReady("jq_sys_dict_data", "remark");
        mockColumnReady("jq_sys_dict_data", "created_by");
        mockColumnReady("jq_sys_dict_data", "updated_by");
        mockColumnReady("jq_sys_dict_data", "created_at");
        mockColumnReady("jq_sys_dict_data", "updated_at");
        mockDictTypeReady("sys_common_status");
        mockDictTypeReady("sys_dept_status");
        mockDictDataReady("sys_common_status", "1");
        mockDictDataReady("sys_common_status", "0");
        mockDictDataReady("sys_dept_status", "1");
        mockDictDataReady("sys_dept_status", "0");

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate, never()).execute(eq("CREATE TABLE IF NOT EXISTS jq_sys_dict_type (id BIGINT PRIMARY KEY AUTO_INCREMENT,dict_name VARCHAR(64) NOT NULL,dict_type VARCHAR(64) NOT NULL,status TINYINT NOT NULL DEFAULT 1,remark VARCHAR(255) NOT NULL DEFAULT '',created_by BIGINT NOT NULL DEFAULT 0,updated_by BIGINT NOT NULL DEFAULT 0,created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,UNIQUE KEY uk_jq_sys_dict_type_type (dict_type)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"));
    }

    private void mockColumnMissing(String tableName, String columnName) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq(tableName), eq(columnName))).thenReturn(0);
    }

    private void mockColumnReady(String tableName, String columnName) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq(tableName), eq(columnName))).thenReturn(1);
    }

    private void mockDictTypeMissing(String dictType) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM jq_sys_dict_type WHERE dict_type = ?"),
                eq(Integer.class), eq(dictType))).thenReturn(0);
    }

    private void mockDictTypeReady(String dictType) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM jq_sys_dict_type WHERE dict_type = ?"),
                eq(Integer.class), eq(dictType))).thenReturn(1);
    }

    private void mockDictDataMissing(String dictType, String value) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM jq_sys_dict_data WHERE dict_type = ? AND value = ?"),
                eq(Integer.class), eq(dictType), eq(value))).thenReturn(0);
    }

    private void mockDictDataReady(String dictType, String value) {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM jq_sys_dict_data WHERE dict_type = ? AND value = ?"),
                eq(Integer.class), eq(dictType), eq(value))).thenReturn(1);
    }
}

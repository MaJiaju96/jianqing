package com.jianqing.module.system.startup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigSchemaInitializerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldRepairLegacyConfigSchemaWhenColumnsMissing() throws Exception {
        ConfigSchemaInitializer initializer = new ConfigSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_config"), eq("config_group")))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_config"), eq("value_type")))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_config"), eq("is_builtin")))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_config_history")))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_config_history"), eq("config_group")))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_config_history"), eq("value_type")))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_config_history"), eq("is_builtin")))
                .thenReturn(0);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        InOrder inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_config ADD COLUMN config_group VARCHAR(64) NOT NULL DEFAULT 'DEFAULT_GROUP' AFTER config_name");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_config ADD COLUMN value_type VARCHAR(32) NOT NULL DEFAULT 'string' AFTER config_group");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_config ADD COLUMN is_builtin TINYINT NOT NULL DEFAULT 0 AFTER value_type");
        inOrder.verify(jdbcTemplate).execute(anyString());
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_config_history ADD COLUMN config_group VARCHAR(64) NOT NULL DEFAULT 'DEFAULT_GROUP' AFTER config_name");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_config_history ADD COLUMN value_type VARCHAR(32) NOT NULL DEFAULT 'string' AFTER config_value");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE jq_sys_config_history ADD COLUMN is_builtin TINYINT NOT NULL DEFAULT 0 AFTER value_type");
    }

    @Test
    void shouldSkipRepairWhenColumnsAlreadyExist() throws Exception {
        ConfigSchemaInitializer initializer = new ConfigSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"),
                eq(Integer.class), eq("jianqing"), anyString(), anyString()))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_sys_config_history")))
                .thenReturn(1);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate, never()).execute(anyString());
    }
}

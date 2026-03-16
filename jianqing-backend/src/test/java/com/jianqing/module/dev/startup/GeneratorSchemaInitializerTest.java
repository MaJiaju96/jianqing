package com.jianqing.module.dev.startup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneratorSchemaInitializerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateWriteRecordTableWhenMissing() throws Exception {
        GeneratorSchemaInitializer initializer = new GeneratorSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_dev_gen_write_record")))
                .thenReturn(0);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate).execute(anyString());
    }

    @Test
    void shouldSkipCreateWhenWriteRecordTableExists() throws Exception {
        GeneratorSchemaInitializer initializer = new GeneratorSchemaInitializer(jdbcTemplate);
        when(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class)).thenReturn("jianqing");
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"),
                eq(Integer.class), eq("jianqing"), eq("jq_dev_gen_write_record")))
                .thenReturn(1);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate, never()).execute(anyString());
    }
}

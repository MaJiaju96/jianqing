package com.jianqing.module.dev.service.impl;

import com.jianqing.module.dev.dto.GenColumnMeta;
import com.jianqing.module.dev.dto.GenTableSummary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneratorMetadataServiceImplTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Test
    void shouldListTables() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("jianqing");
        when(connection.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("table_name")).thenReturn("jq_demo_customer");
        when(resultSet.getString("table_comment")).thenReturn("客户表");

        GeneratorMetadataServiceImpl service = new GeneratorMetadataServiceImpl(dataSource);
        List<GenTableSummary> tables = service.listTables();

        Assertions.assertEquals(1, tables.size());
        Assertions.assertEquals("jq_demo_customer", tables.get(0).getTableName());
        Assertions.assertEquals("客户表", tables.get(0).getTableComment());
        verify(preparedStatement).setString(1, "jianqing");
    }

    @Test
    void shouldListColumns() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("jianqing");
        when(connection.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("column_name")).thenReturn("id");
        when(resultSet.getString("column_comment")).thenReturn("主键");
        when(resultSet.getString("data_type")).thenReturn("bigint");
        when(resultSet.getString("column_type")).thenReturn("bigint(20)");
        when(resultSet.getString("is_nullable")).thenReturn("NO");
        when(resultSet.getString("column_key")).thenReturn("PRI");
        when(resultSet.getString("extra")).thenReturn("auto_increment");

        GeneratorMetadataServiceImpl service = new GeneratorMetadataServiceImpl(dataSource);
        List<GenColumnMeta> columns = service.listColumns("jq_demo_customer");

        Assertions.assertEquals(1, columns.size());
        Assertions.assertEquals("id", columns.get(0).getColumnName());
        Assertions.assertEquals("Long", columns.get(0).getJavaType());
        Assertions.assertFalse(columns.get(0).getNullable());
        Assertions.assertTrue(columns.get(0).getPrimaryKey());
        Assertions.assertTrue(columns.get(0).getAutoIncrement());
        verify(preparedStatement).setString(1, "jianqing");
        verify(preparedStatement).setString(2, "jq_demo_customer");
    }

    @Test
    void shouldRejectIllegalTableName() {
        GeneratorMetadataServiceImpl service = new GeneratorMetadataServiceImpl(dataSource);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> service.listColumns("jq_demo_customer;drop table"));

        Assertions.assertEquals("表名不合法", exception.getMessage());
    }
}

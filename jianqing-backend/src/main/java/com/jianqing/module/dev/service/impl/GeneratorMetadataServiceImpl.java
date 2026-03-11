package com.jianqing.module.dev.service.impl;

import com.jianqing.module.dev.dto.GenColumnMeta;
import com.jianqing.module.dev.dto.GenTableSummary;
import com.jianqing.module.dev.service.GeneratorMetadataService;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class GeneratorMetadataServiceImpl implements GeneratorMetadataService {

    private static final String TABLES_SQL = "SELECT table_name, table_comment FROM information_schema.tables "
            + "WHERE table_schema = ? AND table_type = 'BASE TABLE' ORDER BY table_name";
    private static final String COLUMNS_SQL = "SELECT column_name, column_comment, data_type, column_type, is_nullable, column_key, extra "
            + "FROM information_schema.columns WHERE table_schema = ? AND table_name = ? ORDER BY ordinal_position";

    private final DataSource dataSource;

    public GeneratorMetadataServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<GenTableSummary> listTables() {
        try (Connection connection = dataSource.getConnection()) {
            String schemaName = requireSchemaName(connection);
            try (PreparedStatement statement = connection.prepareStatement(TABLES_SQL)) {
                statement.setString(1, schemaName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<GenTableSummary> tables = new ArrayList<>();
                    while (resultSet.next()) {
                        GenTableSummary table = new GenTableSummary();
                        table.setTableName(resultSet.getString("table_name"));
                        table.setTableComment(safeText(resultSet.getString("table_comment")));
                        tables.add(table);
                    }
                    return tables;
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("查询代码生成表元数据失败", exception);
        }
    }

    @Override
    public List<GenColumnMeta> listColumns(String tableName) {
        validateTableName(tableName);
        try (Connection connection = dataSource.getConnection()) {
            String schemaName = requireSchemaName(connection);
            try (PreparedStatement statement = connection.prepareStatement(COLUMNS_SQL)) {
                statement.setString(1, schemaName);
                statement.setString(2, tableName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<GenColumnMeta> columns = new ArrayList<>();
                    while (resultSet.next()) {
                        columns.add(mapColumn(resultSet));
                    }
                    return columns;
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("查询代码生成字段元数据失败", exception);
        }
    }

    private String requireSchemaName(Connection connection) throws SQLException {
        String schemaName = connection.getCatalog();
        if (schemaName == null || schemaName.isBlank()) {
            throw new IllegalStateException("当前数据源未获取到数据库名");
        }
        return schemaName;
    }

    private void validateTableName(String tableName) {
        if (tableName == null || !tableName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("表名不合法");
        }
    }

    private GenColumnMeta mapColumn(ResultSet resultSet) throws SQLException {
        String dataType = resultSet.getString("data_type");
        String columnType = resultSet.getString("column_type");
        String extra = safeText(resultSet.getString("extra")).toLowerCase(Locale.ROOT);
        GenColumnMeta column = new GenColumnMeta();
        column.setColumnName(resultSet.getString("column_name"));
        column.setColumnComment(safeText(resultSet.getString("column_comment")));
        column.setDataType(dataType);
        column.setColumnType(columnType);
        column.setJavaType(resolveJavaType(dataType, columnType));
        column.setNullable("YES".equalsIgnoreCase(resultSet.getString("is_nullable")));
        column.setPrimaryKey("PRI".equalsIgnoreCase(resultSet.getString("column_key")));
        column.setAutoIncrement(extra.contains("auto_increment"));
        return column;
    }

    private String resolveJavaType(String dataType, String columnType) {
        if (dataType == null) {
            return String.class.getSimpleName();
        }
        String normalizedType = dataType.toLowerCase(Locale.ROOT);
        if ("bigint".equals(normalizedType)) {
            return Long.class.getSimpleName();
        }
        if ("int".equals(normalizedType) || "integer".equals(normalizedType)
                || "mediumint".equals(normalizedType) || "smallint".equals(normalizedType)
                || "tinyint".equals(normalizedType)) {
            return Integer.class.getSimpleName();
        }
        if ("decimal".equals(normalizedType) || "numeric".equals(normalizedType)) {
            return BigDecimal.class.getSimpleName();
        }
        if ("double".equals(normalizedType)) {
            return Double.class.getSimpleName();
        }
        if ("float".equals(normalizedType)) {
            return Float.class.getSimpleName();
        }
        if ("date".equals(normalizedType)) {
            return LocalDate.class.getSimpleName();
        }
        if ("time".equals(normalizedType)) {
            return LocalTime.class.getSimpleName();
        }
        if ("datetime".equals(normalizedType) || "timestamp".equals(normalizedType)) {
            return LocalDateTime.class.getSimpleName();
        }
        if ("bit".equals(normalizedType) && columnType != null && columnType.startsWith("bit(1)")) {
            return Boolean.class.getSimpleName();
        }
        if (normalizedType.contains("blob") || normalizedType.contains("binary")) {
            return "byte[]";
        }
        return String.class.getSimpleName();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}

package com.jianqing.module.dev.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class GeneratorSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public GeneratorSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String schemaName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (schemaName == null || schemaName.trim().isEmpty()) {
            return;
        }
        ensureWriteRecordTable(schemaName);
    }

    private void ensureWriteRecordTable(String schemaName) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?",
                Integer.class, schemaName, "jq_dev_gen_write_record");
        if (exists != null && exists > 0) {
            return;
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS jq_dev_gen_write_record ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "marker_id VARCHAR(64) NOT NULL,"
                + "table_name VARCHAR(128) NOT NULL,"
                + "module_name VARCHAR(64) NOT NULL,"
                + "business_name VARCHAR(128) NOT NULL,"
                + "class_name VARCHAR(128) NOT NULL,"
                + "perm_prefix VARCHAR(128) NOT NULL,"
                + "username VARCHAR(64) DEFAULT NULL,"
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uk_marker_id (marker_id),"
                + "KEY idx_created_at (created_at)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='代码生成写入记录'");
    }
}

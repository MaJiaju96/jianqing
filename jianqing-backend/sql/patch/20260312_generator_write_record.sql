CREATE TABLE IF NOT EXISTS jq_dev_gen_write_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    marker_id VARCHAR(64) NOT NULL,
    table_name VARCHAR(128) NOT NULL,
    module_name VARCHAR(64) NOT NULL,
    business_name VARCHAR(128) NOT NULL,
    class_name VARCHAR(128) NOT NULL,
    perm_prefix VARCHAR(128) NOT NULL,
    username VARCHAR(64) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_marker_id (marker_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='代码生成写入记录';

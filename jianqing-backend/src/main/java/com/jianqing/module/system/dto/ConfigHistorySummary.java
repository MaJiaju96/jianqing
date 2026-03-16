package com.jianqing.module.system.dto;

public record ConfigHistorySummary(Long id, Long configId, String configKey, String configName,
                                   String configGroup, String configValue, String valueType,
                                   Integer isBuiltin, String changeType, String changeNote, String createdAt) {
}

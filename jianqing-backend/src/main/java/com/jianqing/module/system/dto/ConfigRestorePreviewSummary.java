package com.jianqing.module.system.dto;

import java.util.List;

public record ConfigRestorePreviewSummary(Long historyId, String configKey, String configName,
                                          String configGroup, String configValue, String valueType,
                                          Integer isBuiltin, String changeNote, String historyCreatedAt,
                                          Boolean keyOccupied, Long existingConfigId, String existingConfigName,
                                          List<ConfigDiffItem> items) {
}

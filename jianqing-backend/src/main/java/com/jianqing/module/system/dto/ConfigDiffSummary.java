package com.jianqing.module.system.dto;

import java.util.List;

public record ConfigDiffSummary(Long configId, Long historyId, Long compareHistoryId, String configKey,
                                String historyChangeType, String historyCreatedAt,
                                String compareChangeType, String compareCreatedAt,
                                Boolean compareWithCurrent,
                                List<ConfigDiffItem> items) {
}

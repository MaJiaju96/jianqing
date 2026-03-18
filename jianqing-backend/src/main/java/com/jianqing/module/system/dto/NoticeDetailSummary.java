package com.jianqing.module.system.dto;

import java.util.List;

public record NoticeDetailSummary(Long id, String title, String content, String level, String publishMode,
                                  String targetType, List<Long> targetIds, Integer popupEnabled, String status,
                                  String scheduledAt, String publishedAt, String validFrom, String validTo,
                                  String remark) {
}

package com.jianqing.module.system.dto;

public record NoticeSummary(Long id, String title, String level, String publishMode, String targetType,
                            Integer popupEnabled, String status, String scheduledAt, String publishedAt,
                            String validFrom, String validTo, String remark) {
}

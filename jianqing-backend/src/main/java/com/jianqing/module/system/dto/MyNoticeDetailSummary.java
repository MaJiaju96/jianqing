package com.jianqing.module.system.dto;

public record MyNoticeDetailSummary(Long noticeId, String title, String content, String level, Integer popupEnabled,
                                    Integer readStatus, String publishedAt, String readAt, String validFrom,
                                    String validTo) {
}

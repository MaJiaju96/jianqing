package com.jianqing.module.system.dto;

public record MyNoticeSummary(Long noticeId, String title, String content, String level, Integer popupEnabled, Integer readStatus,
                               String publishedAt, String readAt) {
}

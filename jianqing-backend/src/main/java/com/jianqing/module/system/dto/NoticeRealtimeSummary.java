package com.jianqing.module.system.dto;

import java.util.List;

public record NoticeRealtimeSummary(Long unreadCount, List<MyNoticeSummary> latestNotices) {
}

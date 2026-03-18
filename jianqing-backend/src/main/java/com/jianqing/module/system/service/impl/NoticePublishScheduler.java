package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.service.NoticeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NoticePublishScheduler {

    private final NoticeService noticeService;

    public NoticePublishScheduler(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @Scheduled(fixedDelay = 60000L, initialDelay = 30000L)
    public void publishPendingNotices() {
        noticeService.publishPendingNotices();
    }
}

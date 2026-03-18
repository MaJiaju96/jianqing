package com.jianqing.module.system.service;

import com.jianqing.module.system.dto.MyNoticeDetailSummary;
import com.jianqing.module.system.dto.MyNoticeSummary;
import com.jianqing.module.system.dto.NoticeDetailSummary;
import com.jianqing.module.system.dto.NoticeRealtimeSummary;
import com.jianqing.module.system.dto.NoticeSaveRequest;
import com.jianqing.module.system.dto.NoticeSummary;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface NoticeService {

    List<NoticeSummary> listNotices();

    List<NoticeSummary> listTrashNotices(String category);

    NoticeDetailSummary getNoticeDetail(Long id);

    NoticeDetailSummary createNotice(NoticeSaveRequest request);

    NoticeDetailSummary updateNotice(Long id, NoticeSaveRequest request);

    NoticeDetailSummary publishNotice(Long id);

    NoticeDetailSummary cancelNotice(Long id);

    void deleteNotice(Long id);

    NoticeDetailSummary restoreNotice(Long id);

    void purgeNotice(Long id);

    List<MyNoticeSummary> listMyNotices(Long userId);

    MyNoticeDetailSummary getMyNoticeDetail(Long userId, Long noticeId);

    Long countUnreadNotices(Long userId);

    List<MyNoticeSummary> listLatestNotices(Long userId, int limit);

    List<MyNoticeSummary> listPopupCandidates(Long userId, int limit);

    void markNoticeRead(Long userId, Long noticeId);

    void markAllNoticesRead(Long userId);

    SseEmitter subscribeNoticeStream(Long userId);

    NoticeRealtimeSummary getNoticeRealtimeSummary(Long userId);

    void publishPendingNotices();
}

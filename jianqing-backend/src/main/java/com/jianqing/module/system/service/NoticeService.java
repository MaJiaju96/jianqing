package com.jianqing.module.system.service;

import com.jianqing.module.system.dto.MyNoticeDetailSummary;
import com.jianqing.module.system.dto.MyNoticeSummary;
import com.jianqing.module.system.dto.NoticeDetailSummary;
import com.jianqing.module.system.dto.NoticeSaveRequest;
import com.jianqing.module.system.dto.NoticeSummary;

import java.util.List;

public interface NoticeService {

    List<NoticeSummary> listNotices();

    NoticeDetailSummary getNoticeDetail(Long id);

    NoticeDetailSummary createNotice(NoticeSaveRequest request);

    NoticeDetailSummary updateNotice(Long id, NoticeSaveRequest request);

    NoticeDetailSummary publishNotice(Long id);

    NoticeDetailSummary cancelNotice(Long id);

    void deleteNotice(Long id);

    List<MyNoticeSummary> listMyNotices(Long userId);

    MyNoticeDetailSummary getMyNoticeDetail(Long userId, Long noticeId);

    Long countUnreadNotices(Long userId);

    List<MyNoticeSummary> listLatestNotices(Long userId, int limit);

    List<MyNoticeSummary> listPopupCandidates(Long userId, int limit);

    void markNoticeRead(Long userId, Long noticeId);

    void markAllNoticesRead(Long userId);

    void publishPendingNotices();
}

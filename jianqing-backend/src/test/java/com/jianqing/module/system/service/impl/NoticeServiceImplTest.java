package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.dto.MyNoticeDetailSummary;
import com.jianqing.module.system.dto.NoticeInboxRow;
import com.jianqing.module.system.dto.NoticeSaveRequest;
import com.jianqing.module.system.dto.NoticeSummary;
import com.jianqing.module.system.entity.SysNotice;
import com.jianqing.module.system.entity.SysNoticeUser;
import com.jianqing.module.system.mapper.SysNoticeMapper;
import com.jianqing.module.system.mapper.SysNoticeTargetMapper;
import com.jianqing.module.system.mapper.SysNoticeUserMapper;
import com.jianqing.module.system.mapper.SysUserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeServiceImplTest {

    @Mock
    private SysNoticeMapper sysNoticeMapper;

    @Mock
    private SysNoticeTargetMapper sysNoticeTargetMapper;

    @Mock
    private SysNoticeUserMapper sysNoticeUserMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    private NoticeServiceImpl noticeService;

    @BeforeEach
    void setUp() {
        noticeService = new NoticeServiceImpl(sysNoticeMapper, sysNoticeTargetMapper, sysNoticeUserMapper, sysUserMapper);
    }

    @Test
    void shouldCreateScheduledNoticeAsPending() {
        when(sysNoticeMapper.insert(any(SysNotice.class))).thenAnswer(invocation -> {
            SysNotice notice = invocation.getArgument(0);
            notice.setId(11L);
            return 1;
        });
        when(sysNoticeTargetMapper.selectTargetIdsByNoticeId(11L)).thenReturn(List.of(2L, 3L));

        NoticeSaveRequest request = buildScheduledRequest();
        request.setTargetType("dept");
        request.setTargetIds(List.of(2L, 3L));

        Assertions.assertEquals("PENDING", noticeService.createNotice(request).status());

        ArgumentCaptor<SysNotice> captor = ArgumentCaptor.forClass(SysNotice.class);
        verify(sysNoticeMapper).insert(captor.capture());
        Assertions.assertEquals("SCHEDULED", captor.getValue().getPublishMode());
        Assertions.assertEquals("PENDING", captor.getValue().getStatus());
        verify(sysNoticeTargetMapper).batchInsert(11L, "DEPT", List.of(2L, 3L));
    }

    @Test
    void shouldPublishImmediateNoticeToExplicitUsers() {
        SysNotice notice = new SysNotice();
        notice.setId(9L);
        notice.setTitle("系统维护通知");
        notice.setContent("今晚 22:00 维护");
        notice.setLevel("IMPORTANT");
        notice.setPublishMode("IMMEDIATE");
        notice.setTargetType("USER");
        notice.setPopupEnabled(1);
        notice.setStatus("DRAFT");
        when(sysNoticeMapper.selectById(9L)).thenReturn(notice);
        when(sysNoticeTargetMapper.selectTargetIdsByNoticeId(9L)).thenReturn(List.of(1L, 5L));
        when(sysUserMapper.selectExistingActiveUserIds(List.of(1L, 5L))).thenReturn(List.of(1L, 5L));

        Assertions.assertEquals("PUBLISHED", noticeService.publishNotice(9L).status());

        ArgumentCaptor<List<SysNoticeUser>> captor = ArgumentCaptor.forClass(List.class);
        verify(sysNoticeUserMapper).batchInsert(captor.capture());
        Assertions.assertEquals(2, captor.getValue().size());
        verify(sysNoticeMapper).updateById(notice);
        Assertions.assertEquals("PUBLISHED", notice.getStatus());
        Assertions.assertNotNull(notice.getPublishedAt());
    }

    @Test
    void shouldListNotices() {
        SysNotice notice = new SysNotice();
        notice.setId(1L);
        notice.setTitle("版本发布");
        notice.setLevel("NORMAL");
        notice.setPublishMode("IMMEDIATE");
        notice.setTargetType("ALL");
        notice.setPopupEnabled(0);
        notice.setStatus("DRAFT");
        when(sysNoticeMapper.selectAllNotices()).thenReturn(List.of(notice));

        List<NoticeSummary> result = noticeService.listNotices();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("版本发布", result.get(0).title());
    }

    @Test
    void shouldMarkNoticeReadWhenOpenDetail() {
        NoticeInboxRow row = new NoticeInboxRow();
        row.setNoticeId(7L);
        row.setTitle("系统升级");
        row.setContent("升级说明");
        row.setLevel("URGENT");
        row.setPopupEnabled(1);
        row.setReadStatus(0);
        row.setPublishedAt(LocalDateTime.now());
        when(sysNoticeUserMapper.selectMyNoticeById(1L, 7L)).thenReturn(row);

        MyNoticeDetailSummary summary = noticeService.getMyNoticeDetail(1L, 7L);

        Assertions.assertEquals(7L, summary.noticeId());
        Assertions.assertEquals(1, summary.readStatus());
        verify(sysNoticeUserMapper).markRead(eq(1L), eq(7L), any(LocalDateTime.class));
    }

    @Test
    void shouldPublishPendingScheduledNotices() {
        SysNotice notice = new SysNotice();
        notice.setId(12L);
        notice.setPublishMode("SCHEDULED");
        notice.setTargetType("ALL");
        notice.setStatus("PENDING");
        when(sysNoticeMapper.selectPendingPublishNotices(any(LocalDateTime.class))).thenReturn(List.of(notice));
        when(sysNoticeTargetMapper.selectTargetIdsByNoticeId(12L)).thenReturn(List.of(0L));
        when(sysUserMapper.selectAllActiveUserIds()).thenReturn(List.of(1L, 2L));

        noticeService.publishPendingNotices();

        verify(sysNoticeUserMapper).batchInsert(any(List.class));
        verify(sysNoticeMapper).updateById(notice);
        Assertions.assertEquals("PUBLISHED", notice.getStatus());
    }

    private NoticeSaveRequest buildScheduledRequest() {
        NoticeSaveRequest request = new NoticeSaveRequest();
        request.setTitle("排期通知");
        request.setContent("明日上线");
        request.setLevel("important");
        request.setPublishMode("scheduled");
        request.setScheduledAt(LocalDateTime.now().plusHours(2));
        request.setPopupEnabled(1);
        request.setValidFrom(LocalDateTime.now());
        request.setValidTo(LocalDateTime.now().plusDays(3));
        request.setRemark("请关注");
        return request;
    }
}

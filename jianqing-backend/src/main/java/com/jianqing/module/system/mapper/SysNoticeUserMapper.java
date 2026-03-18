package com.jianqing.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianqing.module.system.dto.NoticeInboxRow;
import com.jianqing.module.system.entity.SysNoticeUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SysNoticeUserMapper extends BaseMapper<SysNoticeUser> {

    void deleteByNoticeId(@Param("noticeId") Long noticeId);

    void batchInsert(@Param("items") List<SysNoticeUser> items);

    List<NoticeInboxRow> selectMyNotices(@Param("userId") Long userId);

    NoticeInboxRow selectMyNoticeById(@Param("userId") Long userId, @Param("noticeId") Long noticeId);

    Long countUnreadByUserId(@Param("userId") Long userId);

    List<NoticeInboxRow> selectLatestByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    List<NoticeInboxRow> selectPopupCandidates(@Param("userId") Long userId, @Param("now") LocalDateTime now,
                                               @Param("limit") int limit);

    List<Long> selectUserIdsByNoticeId(@Param("noticeId") Long noticeId);

    int markRead(@Param("userId") Long userId, @Param("noticeId") Long noticeId, @Param("readAt") LocalDateTime readAt);

    int markAllRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);
}

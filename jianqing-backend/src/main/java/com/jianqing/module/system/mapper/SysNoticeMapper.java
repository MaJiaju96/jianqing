package com.jianqing.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianqing.module.system.entity.SysNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {

    List<SysNotice> selectAllNotices();

    List<SysNotice> selectTrashNotices(@Param("category") String category);

    List<SysNotice> selectPendingPublishNotices(@Param("now") LocalDateTime now);
}

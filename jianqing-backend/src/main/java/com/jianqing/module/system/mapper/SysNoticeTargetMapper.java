package com.jianqing.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianqing.module.system.entity.SysNoticeTarget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysNoticeTargetMapper extends BaseMapper<SysNoticeTarget> {

    void deleteByNoticeId(@Param("noticeId") Long noticeId);

    void batchInsert(@Param("noticeId") Long noticeId, @Param("targetType") String targetType,
                     @Param("targetIds") List<Long> targetIds);

    List<Long> selectTargetIdsByNoticeId(@Param("noticeId") Long noticeId);
}

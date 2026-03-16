package com.jianqing.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianqing.module.system.entity.SysConfigHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysConfigHistoryMapper extends BaseMapper<SysConfigHistory> {

    List<SysConfigHistory> selectByConfigId(@Param("configId") Long configId);

    SysConfigHistory selectByIdAndConfigId(@Param("id") Long id, @Param("configId") Long configId);

    List<SysConfigHistory> selectLatestDeletedHistories();

    SysConfigHistory selectDeleteHistoryById(@Param("id") Long id);
}

package com.jianqing.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianqing.module.system.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {

    List<SysConfig> selectAllConfigs();

    Long countByConfigKey(@Param("configKey") String configKey, @Param("excludeId") Long excludeId);

    SysConfig selectByConfigKey(@Param("configKey") String configKey);
}

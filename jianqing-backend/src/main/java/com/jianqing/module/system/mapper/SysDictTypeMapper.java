package com.jianqing.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianqing.module.system.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {

    List<SysDictType> selectAllDictTypes();

    Long countByDictType(@Param("dictType") String dictType, @Param("excludeId") Long excludeId);
}

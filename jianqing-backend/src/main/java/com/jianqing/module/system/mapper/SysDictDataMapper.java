package com.jianqing.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianqing.module.system.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    List<SysDictData> selectByDictType(@Param("dictType") String dictType);

    List<SysDictData> selectEnabledByDictType(@Param("dictType") String dictType);

    Long countByDictType(@Param("dictType") String dictType);

    Long countByDictTypeAndValue(@Param("dictType") String dictType,
                                 @Param("value") String value,
                                 @Param("excludeId") Long excludeId);

    int updateDictType(@Param("oldDictType") String oldDictType, @Param("newDictType") String newDictType);
}

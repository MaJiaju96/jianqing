package com.jianqing.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianqing.module.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    List<SysRole> selectEnabledRolesByUserId(@Param("userId") Long userId);

    Long countByRoleCode(@Param("roleCode") String roleCode, @Param("excludeId") Long excludeId);

    void deleteUserRoleByRoleId(@Param("roleId") Long roleId);

    void deleteRoleMenuByRoleId(@Param("roleId") Long roleId);

    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    void batchInsertRoleMenu(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
}

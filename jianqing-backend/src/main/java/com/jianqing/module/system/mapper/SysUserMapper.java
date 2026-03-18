package com.jianqing.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianqing.module.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    Long selectIdByUsername(@Param("username") String username);

    Long countByUsername(@Param("username") String username, @Param("excludeId") Long excludeId);

    void deleteUserRoleByUserId(@Param("userId") Long userId);

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    List<Long> selectUserIdsByMenuId(@Param("menuId") Long menuId);

    List<Long> selectAllActiveUserIds();

    List<Long> selectActiveUserIdsByRoleIds(@Param("roleIds") List<Long> roleIds);

    List<Long> selectActiveUserIdsByDeptIds(@Param("deptIds") List<Long> deptIds);

    List<Long> selectExistingActiveUserIds(@Param("userIds") List<Long> userIds);

    void batchInsertUserRole(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}

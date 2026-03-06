package com.jianqing.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianqing.module.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> selectEnabledMenusByUserId(@Param("userId") Long userId);

    List<SysMenu> selectPermMenusByUserId(@Param("userId") Long userId);

    List<SysMenu> selectAllEnabledMenus();

    Long countChildren(@Param("menuId") Long menuId);

    void deleteRoleMenuByMenuId(@Param("menuId") Long menuId);
}

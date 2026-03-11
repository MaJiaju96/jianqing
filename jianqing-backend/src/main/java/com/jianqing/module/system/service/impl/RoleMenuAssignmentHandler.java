package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.RoleService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleMenuAssignmentHandler {

    private final SysUserMapper sysUserMapper;
    private final RoleService roleService;
    private final SystemCacheEvictor systemCacheEvictor;

    public RoleMenuAssignmentHandler(SysUserMapper sysUserMapper,
                                     RoleService roleService,
                                     SystemCacheEvictor systemCacheEvictor) {
        this.sysUserMapper = sysUserMapper;
        this.roleService = roleService;
        this.systemCacheEvictor = systemCacheEvictor;
    }

    public void assignRoleMenus(Long roleId, List<Long> menuIds) {
        List<Long> affectedUserIds = sysUserMapper.selectUserIdsByRoleId(roleId);
        roleService.assignRoleMenus(roleId, menuIds);
        systemCacheEvictor.evictUserAuthBatch(affectedUserIds);
    }
}

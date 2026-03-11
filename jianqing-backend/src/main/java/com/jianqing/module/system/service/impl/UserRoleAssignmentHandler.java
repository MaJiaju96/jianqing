package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.RoleService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserRoleAssignmentHandler {

    private final SysUserMapper sysUserMapper;
    private final RoleService roleService;
    private final SystemCacheEvictor systemCacheEvictor;

    public UserRoleAssignmentHandler(SysUserMapper sysUserMapper,
                                     RoleService roleService,
                                     SystemCacheEvictor systemCacheEvictor) {
        this.sysUserMapper = sysUserMapper;
        this.roleService = roleService;
        this.systemCacheEvictor = systemCacheEvictor;
    }

    public void assignUserRoles(Long userId, List<Long> roleIds) {
        List<Long> validRoleIds = roleIds == null ? Collections.emptyList() : roleIds.stream().distinct().toList();
        roleService.validateRoleIds(validRoleIds);
        sysUserMapper.deleteUserRoleByUserId(userId);
        if (!validRoleIds.isEmpty()) {
            sysUserMapper.batchInsertUserRole(userId, validRoleIds);
        }
        systemCacheEvictor.evictUserAuth(userId);
    }
}

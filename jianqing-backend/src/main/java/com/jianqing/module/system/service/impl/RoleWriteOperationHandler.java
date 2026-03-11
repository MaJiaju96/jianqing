package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.dto.RoleSaveRequest;
import com.jianqing.module.system.dto.RoleSummary;
import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.RoleService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleWriteOperationHandler {

    private final SysUserMapper sysUserMapper;
    private final RoleService roleService;
    private final SystemCacheEvictor systemCacheEvictor;

    public RoleWriteOperationHandler(SysUserMapper sysUserMapper,
                                     RoleService roleService,
                                     SystemCacheEvictor systemCacheEvictor) {
        this.sysUserMapper = sysUserMapper;
        this.roleService = roleService;
        this.systemCacheEvictor = systemCacheEvictor;
    }

    public RoleSummary createRole(RoleSaveRequest request) {
        RoleSummary summary = roleService.createRole(request);
        systemCacheEvictor.evictSystemRoles();
        return summary;
    }

    public RoleSummary updateRole(Long id, RoleSaveRequest request) {
        List<Long> affectedUserIds = sysUserMapper.selectUserIdsByRoleId(id);
        RoleSummary summary = roleService.updateRole(id, request);
        systemCacheEvictor.evictSystemRoles();
        systemCacheEvictor.evictUserAuthBatch(affectedUserIds);
        return summary;
    }

    public void deleteRole(Long id) {
        List<Long> affectedUserIds = sysUserMapper.selectUserIdsByRoleId(id);
        roleService.deleteRole(id);
        systemCacheEvictor.evictSystemRoles();
        systemCacheEvictor.evictUserAuthBatch(affectedUserIds);
    }
}

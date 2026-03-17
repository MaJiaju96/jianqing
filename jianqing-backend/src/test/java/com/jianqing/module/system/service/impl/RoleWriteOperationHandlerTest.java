package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.dto.RoleSaveRequest;
import com.jianqing.module.system.dto.RoleSummary;
import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.RoleService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleWriteOperationHandlerTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private RoleService roleService;

    @Mock
    private SystemCacheEvictor systemCacheEvictor;

    @Test
    void shouldCreateRoleAndEvictSystemRoles() {
        RoleWriteOperationHandler handler = new RoleWriteOperationHandler(sysUserMapper, roleService, systemCacheEvictor);
        RoleSaveRequest request = new RoleSaveRequest();
        RoleSummary summary = new RoleSummary(1L, "管理员", "ADMIN", 1, 1, Collections.emptyList());
        when(roleService.createRole(request)).thenReturn(summary);

        RoleSummary actual = handler.createRole(request);

        verify(roleService).createRole(request);
        verify(systemCacheEvictor).evictSystemRoles();
        Assertions.assertSame(summary, actual);
    }

    @Test
    void shouldUpdateRoleAndEvictAffectedUserAuth() {
        RoleWriteOperationHandler handler = new RoleWriteOperationHandler(sysUserMapper, roleService, systemCacheEvictor);
        RoleSaveRequest request = new RoleSaveRequest();
        RoleSummary summary = new RoleSummary(2L, "审计员", "AUDITOR", 2, 1, Collections.emptyList());
        when(sysUserMapper.selectUserIdsByRoleId(2L)).thenReturn(Arrays.asList(9L, 10L));
        when(roleService.updateRole(2L, request)).thenReturn(summary);

        RoleSummary actual = handler.updateRole(2L, request);

        verify(roleService).updateRole(2L, request);
        verify(systemCacheEvictor).evictSystemRoles();
        verify(systemCacheEvictor).evictUserAuthBatch(Arrays.asList(9L, 10L));
        Assertions.assertSame(summary, actual);
    }

    @Test
    void shouldDeleteRoleAndEvictAffectedUserAuth() {
        RoleWriteOperationHandler handler = new RoleWriteOperationHandler(sysUserMapper, roleService, systemCacheEvictor);
        when(sysUserMapper.selectUserIdsByRoleId(3L)).thenReturn(Arrays.asList(8L, 9L));

        handler.deleteRole(3L);

        verify(roleService).deleteRole(3L);
        verify(systemCacheEvictor).evictSystemRoles();
        verify(systemCacheEvictor).evictUserAuthBatch(Arrays.asList(8L, 9L));
    }
}

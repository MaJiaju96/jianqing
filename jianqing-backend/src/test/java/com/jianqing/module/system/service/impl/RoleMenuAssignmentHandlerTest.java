package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleMenuAssignmentHandlerTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private RoleService roleService;

    @Mock
    private SystemCacheEvictor systemCacheEvictor;

    @Test
    void shouldAssignRoleMenusAndEvictAffectedUsers() {
        RoleMenuAssignmentHandler handler = new RoleMenuAssignmentHandler(sysUserMapper, roleService, systemCacheEvictor);
        when(sysUserMapper.selectUserIdsByRoleId(5L)).thenReturn(Arrays.asList(11L, 12L));

        handler.assignRoleMenus(5L, Arrays.asList(2L, 3L));

        verify(roleService).assignRoleMenus(5L, Arrays.asList(2L, 3L));
        verify(systemCacheEvictor).evictUserAuthBatch(Arrays.asList(11L, 12L));
    }
}

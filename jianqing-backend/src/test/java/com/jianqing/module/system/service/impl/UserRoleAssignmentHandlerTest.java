package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserRoleAssignmentHandlerTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private RoleService roleService;

    @Mock
    private SystemCacheEvictor systemCacheEvictor;

    @Test
    void shouldAssignDistinctRoleIdsAndEvictUserAuth() {
        UserRoleAssignmentHandler handler = new UserRoleAssignmentHandler(sysUserMapper, roleService, systemCacheEvictor);

        handler.assignUserRoles(7L, Arrays.asList(1L, 2L, 1L));

        verify(roleService).validateRoleIds(Arrays.asList(1L, 2L));
        verify(sysUserMapper).deleteUserRoleByUserId(7L);
        verify(sysUserMapper).batchInsertUserRole(7L, Arrays.asList(1L, 2L));
        verify(systemCacheEvictor).evictUserAuth(7L);
    }

    @Test
    void shouldAllowClearingUserRoles() {
        UserRoleAssignmentHandler handler = new UserRoleAssignmentHandler(sysUserMapper, roleService, systemCacheEvictor);

        handler.assignUserRoles(8L, Collections.emptyList());

        verify(roleService).validateRoleIds(Collections.emptyList());
        verify(sysUserMapper).deleteUserRoleByUserId(8L);
        verify(sysUserMapper, never()).batchInsertUserRole(8L, Collections.emptyList());
        verify(systemCacheEvictor).evictUserAuth(8L);
    }
}

package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.constant.DataScopeConstants;
import com.jianqing.module.system.dto.UserSaveRequest;
import com.jianqing.module.system.entity.SysRole;
import com.jianqing.module.system.entity.SysUser;
import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.RoleService;
import com.jianqing.module.system.service.impl.UserDataScopeResolver.CurrentDataScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDataScopeResolverTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private RoleService roleService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldResolveAllScopeForSuperAdmin() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService);
        mockCurrentUser("admin", 1L, 100L);
        when(roleService.listEnabledRolesByUserId(1L)).thenReturn(List.of(buildRole("super_admin", null)));

        CurrentDataScope currentDataScope = resolver.resolveCurrentDataScope();

        Assertions.assertTrue(currentDataScope.all());
        Assertions.assertFalse(currentDataScope.selfOnly());
        Assertions.assertEquals(100L, currentDataScope.deptId());
    }

    @Test
    void shouldResolveDeptScopeForDeptRole() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService);
        mockCurrentUser("dept_user", 2L, 200L);
        when(roleService.listEnabledRolesByUserId(2L))
                .thenReturn(List.of(buildRole("dept_role", DataScopeConstants.DEPT)));

        CurrentDataScope currentDataScope = resolver.resolveCurrentDataScope();

        Assertions.assertFalse(currentDataScope.all());
        Assertions.assertFalse(currentDataScope.selfOnly());
        Assertions.assertEquals(200L, currentDataScope.deptId());
    }

    @Test
    void shouldResolveSelfScopeByDefault() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService);
        mockCurrentUser("self_user", 3L, 300L);
        when(roleService.listEnabledRolesByUserId(3L))
                .thenReturn(List.of(buildRole("self_role", DataScopeConstants.SELF)));

        CurrentDataScope currentDataScope = resolver.resolveCurrentDataScope();

        Assertions.assertFalse(currentDataScope.all());
        Assertions.assertTrue(currentDataScope.selfOnly());
        Assertions.assertEquals(3L, currentDataScope.userId());
    }

    @Test
    void shouldRejectCreateOutsideCurrentDept() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService);
        UserSaveRequest request = new UserSaveRequest();
        request.setDeptId(300L);
        CurrentDataScope currentDataScope = new CurrentDataScope(3L, 200L, false, false);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> resolver.validateUserCreatePermission(request, currentDataScope));

        Assertions.assertEquals("当前数据范围仅允许操作本部门用户", exception.getMessage());
    }

    @Test
    void shouldRejectAccessToOtherUserWhenSelfScopeOnly() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService);
        SysUser targetUser = new SysUser();
        targetUser.setId(8L);

        boolean canAccess = resolver.canAccessUser(targetUser, new CurrentDataScope(3L, 200L, false, true));

        Assertions.assertFalse(canAccess);
    }

    private void mockCurrentUser(String username, Long userId, Long deptId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, List.of()));
        SysUser currentUser = new SysUser();
        currentUser.setId(userId);
        currentUser.setUsername(username);
        currentUser.setDeptId(deptId);
        currentUser.setIsDeleted(0);
        when(sysUserMapper.selectOne(any())).thenReturn(currentUser);
    }

    private SysRole buildRole(String roleCode, Integer dataScope) {
        SysRole role = new SysRole();
        role.setRoleCode(roleCode);
        role.setDataScope(dataScope);
        return role;
    }
}

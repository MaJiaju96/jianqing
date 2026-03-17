package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.constant.DataScopeConstants;
import com.jianqing.module.system.dto.UserSaveRequest;
import com.jianqing.module.system.entity.SysRole;
import com.jianqing.module.system.entity.SysUser;
import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.DeptService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDataScopeResolverTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private RoleService roleService;

    @Mock
    private DeptService deptService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldResolveAllScopeForSuperAdmin() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService, deptService);
        mockCurrentUser("admin", 1L, 100L);
        when(roleService.listEnabledRolesByUserId(1L)).thenReturn(List.of(buildRole("super_admin", null)));

        CurrentDataScope currentDataScope = resolver.resolveCurrentDataScope();

        Assertions.assertTrue(currentDataScope.all());
        Assertions.assertFalse(currentDataScope.selfOnly());
        Assertions.assertEquals(100L, currentDataScope.deptId());
    }

    @Test
    void shouldResolveDeptScopeForDeptRole() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService, deptService);
        mockCurrentUser("dept_user", 2L, 200L);
        when(roleService.listEnabledRolesByUserId(2L))
                .thenReturn(List.of(buildRole("dept_role", DataScopeConstants.DEPT)));

        CurrentDataScope currentDataScope = resolver.resolveCurrentDataScope();

        Assertions.assertFalse(currentDataScope.all());
        Assertions.assertFalse(currentDataScope.selfOnly());
        Assertions.assertEquals(200L, currentDataScope.deptId());
        Assertions.assertEquals(List.of(200L), currentDataScope.deptIds());
    }

    @Test
    void shouldResolveDeptAndChildScopeForDeptTreeRole() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService, deptService);
        mockCurrentUser("dept_tree_user", 5L, 200L);
        when(roleService.listEnabledRolesByUserId(5L))
                .thenReturn(List.of(buildRole("dept_tree_role", DataScopeConstants.DEPT_AND_CHILD)));
        when(deptService.listSelfAndDescendantDeptIds(200L)).thenReturn(List.of(200L, 201L, 202L));

        CurrentDataScope currentDataScope = resolver.resolveCurrentDataScope();

        Assertions.assertFalse(currentDataScope.all());
        Assertions.assertFalse(currentDataScope.selfOnly());
        Assertions.assertEquals(List.of(200L, 201L, 202L), currentDataScope.deptIds());
        verify(deptService).listSelfAndDescendantDeptIds(200L);
    }

    @Test
    void shouldResolveCustomScopeForCustomRole() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService, deptService);
        mockCurrentUser("custom_user", 7L, 200L);
        when(roleService.listEnabledRolesByUserId(7L))
                .thenReturn(List.of(buildRole(11L, "custom_role", DataScopeConstants.CUSTOM)));
        when(roleService.listRoleCustomDeptIds(11L)).thenReturn(List.of(301L, 302L));

        CurrentDataScope currentDataScope = resolver.resolveCurrentDataScope();

        Assertions.assertFalse(currentDataScope.all());
        Assertions.assertFalse(currentDataScope.selfOnly());
        Assertions.assertEquals(List.of(301L, 302L), currentDataScope.deptIds());
    }

    @Test
    void shouldMergeDeptAndCustomScopes() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService, deptService);
        mockCurrentUser("mixed_scope_user", 8L, 200L);
        when(roleService.listEnabledRolesByUserId(8L)).thenReturn(List.of(
                buildRole(12L, "dept_role", DataScopeConstants.DEPT),
                buildRole(13L, "custom_role", DataScopeConstants.CUSTOM)
        ));
        when(roleService.listRoleCustomDeptIds(13L)).thenReturn(List.of(301L, 302L));

        CurrentDataScope currentDataScope = resolver.resolveCurrentDataScope();

        Assertions.assertEquals(List.of(200L, 301L, 302L), currentDataScope.deptIds());
    }

    @Test
    void shouldPreferAllScopeOverDeptAndChildScope() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService, deptService);
        mockCurrentUser("mixed_scope_user", 6L, 200L);
        when(roleService.listEnabledRolesByUserId(6L)).thenReturn(List.of(
                buildRole("dept_tree_role", DataScopeConstants.DEPT_AND_CHILD),
                buildRole("all_role", DataScopeConstants.ALL)
        ));

        CurrentDataScope currentDataScope = resolver.resolveCurrentDataScope();

        Assertions.assertTrue(currentDataScope.all());
        Assertions.assertTrue(currentDataScope.deptIds().isEmpty());
        verify(deptService, never()).listSelfAndDescendantDeptIds(any());
    }

    @Test
    void shouldResolveSelfScopeByDefault() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService, deptService);
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
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService, deptService);
        UserSaveRequest request = new UserSaveRequest();
        request.setDeptId(300L);
        CurrentDataScope currentDataScope = new CurrentDataScope(3L, 200L, List.of(200L), false, false);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> resolver.validateUserCreatePermission(request, currentDataScope));

        Assertions.assertEquals("当前数据范围仅允许操作本部门用户", exception.getMessage());
    }

    @Test
    void shouldRejectAccessToOtherUserWhenSelfScopeOnly() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService, deptService);
        SysUser targetUser = new SysUser();
        targetUser.setId(8L);

        boolean canAccess = resolver.canAccessUser(targetUser,
                new CurrentDataScope(3L, 200L, List.of(), false, true));

        Assertions.assertFalse(canAccess);
    }

    @Test
    void shouldAllowAccessToChildDeptUserWhenDeptAndChildScope() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService, deptService);
        SysUser targetUser = new SysUser();
        targetUser.setId(9L);
        targetUser.setDeptId(201L);

        boolean canAccess = resolver.canAccessUser(targetUser,
                new CurrentDataScope(3L, 200L, List.of(200L, 201L, 202L), false, false));

        Assertions.assertTrue(canAccess);
    }

    @Test
    void shouldRejectAccessToOutsideTreeUserWhenDeptAndChildScope() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService, deptService);
        SysUser targetUser = new SysUser();
        targetUser.setId(10L);
        targetUser.setDeptId(999L);

        boolean canAccess = resolver.canAccessUser(targetUser,
                new CurrentDataScope(3L, 200L, List.of(200L, 201L, 202L), false, false));

        Assertions.assertFalse(canAccess);
    }

    @Test
    void shouldRejectUpdateWhenDeptMovesOutsideCurrentTree() {
        UserDataScopeResolver resolver = new UserDataScopeResolver(sysUserMapper, roleService, deptService);
        UserSaveRequest request = new UserSaveRequest();
        request.setDeptId(999L);
        CurrentDataScope currentDataScope = new CurrentDataScope(3L, 200L, List.of(200L, 201L, 202L), false, false);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> resolver.validateUserUpdatePermission(request, currentDataScope));

        Assertions.assertEquals("当前数据范围仅允许保留在本部门内", exception.getMessage());
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
        return buildRole(null, roleCode, dataScope);
    }

    private SysRole buildRole(Long id, String roleCode, Integer dataScope) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleCode(roleCode);
        role.setDataScope(dataScope);
        return role;
    }
}

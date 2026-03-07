package com.jianqing.module.system.service.impl;

import com.jianqing.framework.cache.CacheConsistencyService;
import com.jianqing.module.system.constant.DataScopeConstants;
import com.jianqing.module.system.dto.UserSaveRequest;
import com.jianqing.module.system.dto.UserSummary;
import com.jianqing.module.system.entity.SysRole;
import com.jianqing.module.system.entity.SysUser;
import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.DeptService;
import com.jianqing.module.system.service.MenuService;
import com.jianqing.module.system.service.RoleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private RoleService roleService;

    @Mock
    private DeptService deptService;

    @Mock
    private MenuService menuService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CacheConsistencyService cacheConsistencyService;

    private SystemServiceImpl systemService;

    @BeforeEach
    void setUp() {
        systemService = new SystemServiceImpl(
                sysUserMapper,
                roleService,
                deptService,
                menuService,
                passwordEncoder,
                cacheConsistencyService
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldListAllUsersWhenCurrentUserIsSuperAdmin() {
        mockCurrentUser("admin", 1L, 1L, List.of(buildRole(DataScopeConstants.SUPER_ADMIN_ROLE_CODE, null)));
        when(sysUserMapper.selectList(any())).thenReturn(List.of(
                buildUser(1L, "admin", 1L),
                buildUser(2L, "outside_user", 3L)
        ));

        List<UserSummary> result = systemService.listUsers();

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("admin", result.get(0).username());
        Assertions.assertEquals("outside_user", result.get(1).username());
    }

    @Test
    void shouldOnlyListSelfWhenCurrentUserHasSelfDataScope() {
        mockCurrentUser("self_user", 4L, 1L, List.of(buildRole("self_scope_role", DataScopeConstants.SELF)));
        when(sysUserMapper.selectList(any())).thenReturn(List.of(buildUser(4L, "self_user", 1L)));

        List<UserSummary> result = systemService.listUsers();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("self_user", result.get(0).username());
    }

    @Test
    void shouldRejectCreateUserWhenCurrentUserHasSelfDataScope() {
        mockCurrentUser("self_user", 4L, 1L, List.of(buildRole("self_scope_role", DataScopeConstants.SELF)));
        UserSaveRequest request = buildUserSaveRequest(1L);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> systemService.createUser(request));

        Assertions.assertEquals("当前数据范围不允许新增用户", exception.getMessage());
        verify(sysUserMapper, never()).insert(isA(SysUser.class));
    }

    @Test
    void shouldRejectUpdateUserOutsideCurrentDept() {
        mockCurrentUser("dept_user", 3L, 1L, List.of(buildRole("dept_scope_role", DataScopeConstants.DEPT)));
        when(sysUserMapper.selectOne(any())).thenReturn(buildCurrentUser(3L, "dept_user", 1L), buildUser(6L, "outside_user", 2L));
        UserSaveRequest request = buildUserSaveRequest(2L);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> systemService.updateUser(6L, request));

        Assertions.assertEquals("当前数据范围不允许访问该用户", exception.getMessage());
        verify(sysUserMapper, never()).updateById(isA(SysUser.class));
    }

    private void mockCurrentUser(String username, Long userId, Long deptId, List<SysRole> roles) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, "N/A", AuthorityUtils.NO_AUTHORITIES)
        );
        when(sysUserMapper.selectOne(any())).thenReturn(buildCurrentUser(userId, username, deptId));
        when(roleService.listEnabledRolesByUserId(eq(userId))).thenReturn(roles);
    }

    private SysUser buildCurrentUser(Long id, String username, Long deptId) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setNickname(username);
        user.setDeptId(deptId);
        user.setStatus(1);
        user.setIsDeleted(0);
        return user;
    }

    private SysUser buildUser(Long id, String username, Long deptId) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setNickname(username);
        user.setRealName(username);
        user.setDeptId(deptId);
        user.setEmail(username + "@jianqing.dev");
        user.setStatus(1);
        user.setIsDeleted(0);
        return user;
    }

    private SysRole buildRole(String roleCode, Integer dataScope) {
        SysRole role = new SysRole();
        role.setId(1L);
        role.setRoleCode(roleCode);
        role.setDataScope(dataScope);
        role.setStatus(1);
        role.setIsDeleted(0);
        return role;
    }

    private UserSaveRequest buildUserSaveRequest(Long deptId) {
        UserSaveRequest request = new UserSaveRequest();
        request.setUsername("new_user");
        request.setPassword("test123456");
        request.setNickname("新用户");
        request.setRealName("新用户");
        request.setMobile("");
        request.setEmail("new_user@jianqing.dev");
        request.setDeptId(deptId);
        request.setStatus(1);
        return request;
    }
}

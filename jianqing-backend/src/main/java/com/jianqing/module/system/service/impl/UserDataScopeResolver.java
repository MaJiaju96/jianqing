package com.jianqing.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianqing.framework.security.SecurityUtils;
import com.jianqing.module.system.constant.DataScopeConstants;
import com.jianqing.module.system.dto.UserSaveRequest;
import com.jianqing.module.system.entity.SysRole;
import com.jianqing.module.system.entity.SysUser;
import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.DeptService;
import com.jianqing.module.system.service.RoleService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserDataScopeResolver {

    private final SysUserMapper sysUserMapper;
    private final RoleService roleService;
    private final DeptService deptService;

    public UserDataScopeResolver(SysUserMapper sysUserMapper, RoleService roleService, DeptService deptService) {
        this.sysUserMapper = sysUserMapper;
        this.roleService = roleService;
        this.deptService = deptService;
    }

    public CurrentDataScope resolveCurrentDataScope() {
        String username = SecurityUtils.currentUsername();
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("当前用户不存在");
        }
        SysUser currentUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getIsDeleted, 0)
                .last("limit 1"));
        if (currentUser == null) {
            throw new IllegalArgumentException("当前用户不存在");
        }
        List<SysRole> roles = roleService.listEnabledRolesByUserId(currentUser.getId());
        if (roles.stream().anyMatch(role -> DataScopeConstants.SUPER_ADMIN_ROLE_CODE.equals(role.getRoleCode()))) {
            return new CurrentDataScope(currentUser.getId(), currentUser.getDeptId(), Collections.emptyList(), true, false);
        }
        if (roles.stream().anyMatch(role -> role.getDataScope() != null && role.getDataScope() == DataScopeConstants.ALL)) {
            return new CurrentDataScope(currentUser.getId(), currentUser.getDeptId(), Collections.emptyList(), true, false);
        }
        if (roles.stream().anyMatch(role -> role.getDataScope() != null
                && role.getDataScope() == DataScopeConstants.DEPT_AND_CHILD)) {
            List<Long> deptIds = deptService.listSelfAndDescendantDeptIds(currentUser.getDeptId());
            return new CurrentDataScope(currentUser.getId(), currentUser.getDeptId(), deptIds, false, false);
        }
        if (roles.stream().anyMatch(role -> role.getDataScope() != null && role.getDataScope() == DataScopeConstants.DEPT)) {
            return new CurrentDataScope(currentUser.getId(), currentUser.getDeptId(), currentUser.getDeptId() == null
                    ? Collections.emptyList() : List.of(currentUser.getDeptId()), false, false);
        }
        return new CurrentDataScope(currentUser.getId(), currentUser.getDeptId(), Collections.emptyList(), false, true);
    }

    public LambdaQueryWrapper<SysUser> buildUserDataScopeQuery(CurrentDataScope currentDataScope) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getIsDeleted, 0);
        if (currentDataScope.all()) {
            return queryWrapper;
        }
        if (currentDataScope.selfOnly()) {
            return queryWrapper.eq(SysUser::getId, currentDataScope.userId());
        }
        if (currentDataScope.deptIds() == null || currentDataScope.deptIds().isEmpty()) {
            return queryWrapper.eq(SysUser::getId, -1L);
        }
        return queryWrapper.in(SysUser::getDeptId, currentDataScope.deptIds());
    }

    public void validateUserCreatePermission(UserSaveRequest request, CurrentDataScope currentDataScope) {
        if (currentDataScope.all()) {
            return;
        }
        if (currentDataScope.selfOnly()) {
            throw new IllegalArgumentException("当前数据范围不允许新增用户");
        }
        if (!canAccessDept(request.getDeptId(), currentDataScope)) {
            throw new IllegalArgumentException("当前数据范围仅允许操作本部门用户");
        }
    }

    public void validateUserUpdatePermission(UserSaveRequest request, CurrentDataScope currentDataScope) {
        if (currentDataScope.all()) {
            return;
        }
        if (!canAccessDept(request.getDeptId(), currentDataScope)) {
            throw new IllegalArgumentException("当前数据范围仅允许保留在本部门内");
        }
    }

    public boolean canAccessUser(SysUser targetUser, CurrentDataScope currentDataScope) {
        if (currentDataScope.all()) {
            return true;
        }
        if (currentDataScope.selfOnly()) {
            return targetUser.getId().equals(currentDataScope.userId());
        }
        return canAccessDept(targetUser.getDeptId(), currentDataScope);
    }

    private boolean canAccessDept(Long deptId, CurrentDataScope currentDataScope) {
        return deptId != null && currentDataScope.deptIds() != null && currentDataScope.deptIds().contains(deptId);
    }

    public record CurrentDataScope(Long userId, Long deptId, List<Long> deptIds, boolean all, boolean selfOnly) {
    }
}

package com.jianqing.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianqing.framework.security.SecurityUtils;
import com.jianqing.module.system.constant.DataScopeConstants;
import com.jianqing.module.system.dto.UserSaveRequest;
import com.jianqing.module.system.entity.SysRole;
import com.jianqing.module.system.entity.SysUser;
import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.RoleService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDataScopeResolver {

    private final SysUserMapper sysUserMapper;
    private final RoleService roleService;

    public UserDataScopeResolver(SysUserMapper sysUserMapper, RoleService roleService) {
        this.sysUserMapper = sysUserMapper;
        this.roleService = roleService;
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
            return new CurrentDataScope(currentUser.getId(), currentUser.getDeptId(), true, false);
        }
        if (roles.stream().anyMatch(role -> role.getDataScope() != null && role.getDataScope() == DataScopeConstants.ALL)) {
            return new CurrentDataScope(currentUser.getId(), currentUser.getDeptId(), true, false);
        }
        if (roles.stream().anyMatch(role -> role.getDataScope() != null && role.getDataScope() == DataScopeConstants.DEPT)) {
            return new CurrentDataScope(currentUser.getId(), currentUser.getDeptId(), false, false);
        }
        return new CurrentDataScope(currentUser.getId(), currentUser.getDeptId(), false, true);
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
        if (currentDataScope.deptId() == null || currentDataScope.deptId() <= 0) {
            return queryWrapper.eq(SysUser::getId, -1L);
        }
        return queryWrapper.eq(SysUser::getDeptId, currentDataScope.deptId());
    }

    public void validateUserCreatePermission(UserSaveRequest request, CurrentDataScope currentDataScope) {
        if (currentDataScope.all()) {
            return;
        }
        if (currentDataScope.selfOnly()) {
            throw new IllegalArgumentException("当前数据范围不允许新增用户");
        }
        if (request.getDeptId() == null || !request.getDeptId().equals(currentDataScope.deptId())) {
            throw new IllegalArgumentException("当前数据范围仅允许操作本部门用户");
        }
    }

    public void validateUserUpdatePermission(UserSaveRequest request, CurrentDataScope currentDataScope) {
        if (currentDataScope.all()) {
            return;
        }
        if (request.getDeptId() == null || !request.getDeptId().equals(currentDataScope.deptId())) {
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
        return targetUser.getDeptId() != null && targetUser.getDeptId().equals(currentDataScope.deptId());
    }

    public record CurrentDataScope(Long userId, Long deptId, boolean all, boolean selfOnly) {
    }
}

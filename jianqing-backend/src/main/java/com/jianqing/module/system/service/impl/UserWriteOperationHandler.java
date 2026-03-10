package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.dto.UserSaveRequest;
import com.jianqing.module.system.entity.SysUser;
import com.jianqing.module.system.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserWriteOperationHandler {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final SystemCacheEvictor systemCacheEvictor;

    public UserWriteOperationHandler(SysUserMapper sysUserMapper,
                                     PasswordEncoder passwordEncoder,
                                     SystemCacheEvictor systemCacheEvictor) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.systemCacheEvictor = systemCacheEvictor;
    }

    public SysUser createUser(UserSaveRequest request) {
        validateCreatePassword(request.getPassword());
        SysUser user = new SysUser();
        applyBasicFields(user, request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsDeleted(0);
        sysUserMapper.insert(user);
        systemCacheEvictor.evictSystemUsers();
        return user;
    }

    public SysUser updateUser(SysUser user, UserSaveRequest request) {
        applyBasicFields(user, request);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        sysUserMapper.updateById(user);
        systemCacheEvictor.evictSystemUsers();
        systemCacheEvictor.evictUserAuth(user.getId());
        return user;
    }

    public void deleteUser(SysUser user) {
        user.setIsDeleted(1);
        sysUserMapper.updateById(user);
        sysUserMapper.deleteUserRoleByUserId(user.getId());
        systemCacheEvictor.evictSystemUsers();
        systemCacheEvictor.evictUserAuth(user.getId());
    }

    private void validateCreatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("新增用户时密码不能为空");
        }
    }

    private void applyBasicFields(SysUser user, UserSaveRequest request) {
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setRealName(safeText(request.getRealName()));
        user.setMobile(safeText(request.getMobile()));
        user.setEmail(safeText(request.getEmail()));
        user.setDeptId(request.getDeptId());
        user.setStatus(request.getStatus());
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}

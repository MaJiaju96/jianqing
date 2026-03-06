package com.jianqing.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianqing.common.api.ApiResponse;
import com.jianqing.framework.security.SecurityUtils;
import com.jianqing.module.system.dto.MenuTreeNode;
import com.jianqing.module.system.dto.MenuSaveRequest;
import com.jianqing.module.system.dto.IdListRequest;
import com.jianqing.module.system.dto.RoleSaveRequest;
import com.jianqing.module.system.dto.RoleSummary;
import com.jianqing.module.system.dto.UserSaveRequest;
import com.jianqing.module.system.dto.UserSummary;
import com.jianqing.module.system.entity.SysUser;
import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.SystemService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final SystemService systemService;
    private final SysUserMapper sysUserMapper;

    public SystemController(SystemService systemService, SysUserMapper sysUserMapper) {
        this.systemService = systemService;
        this.sysUserMapper = sysUserMapper;
    }

    @GetMapping("/users")
    public ApiResponse<List<UserSummary>> users() {
        return ApiResponse.success(systemService.listUsers());
    }

    @PostMapping("/users")
    public ApiResponse<UserSummary> createUser(@Valid @RequestBody UserSaveRequest request) {
        return ApiResponse.success(systemService.createUser(request));
    }

    @PostMapping("/users/{id}/update")
    public ApiResponse<UserSummary> updateUser(@PathVariable Long id, @Valid @RequestBody UserSaveRequest request) {
        return ApiResponse.success(systemService.updateUser(id, request));
    }

    @PostMapping("/users/{id}/delete")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        systemService.deleteUser(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/users/{id}/role-ids")
    public ApiResponse<List<Long>> userRoleIds(@PathVariable Long id) {
        return ApiResponse.success(systemService.listUserRoleIds(id));
    }

    @PostMapping("/users/{id}/roles/assign")
    public ApiResponse<Void> assignUserRoles(@PathVariable Long id, @RequestBody @Valid IdListRequest request) {
        systemService.assignUserRoles(id, request.getIds());
        return ApiResponse.success(null);
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleSummary>> roles() {
        return ApiResponse.success(systemService.listRoles());
    }

    @PostMapping("/roles")
    public ApiResponse<RoleSummary> createRole(@Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.success(systemService.createRole(request));
    }

    @PostMapping("/roles/{id}/update")
    public ApiResponse<RoleSummary> updateRole(@PathVariable Long id, @Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.success(systemService.updateRole(id, request));
    }

    @PostMapping("/roles/{id}/delete")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        systemService.deleteRole(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/roles/{id}/menu-ids")
    public ApiResponse<List<Long>> roleMenuIds(@PathVariable Long id) {
        return ApiResponse.success(systemService.listRoleMenuIds(id));
    }

    @PostMapping("/roles/{id}/menus/assign")
    public ApiResponse<Void> assignRoleMenus(@PathVariable Long id, @RequestBody @Valid IdListRequest request) {
        systemService.assignRoleMenus(id, request.getIds());
        return ApiResponse.success(null);
    }

    @GetMapping("/menus")
    public ApiResponse<List<MenuTreeNode>> menus() {
        return ApiResponse.success(systemService.listAllMenuTree());
    }

    @GetMapping("/menus/tree")
    public ApiResponse<List<MenuTreeNode>> menuTree() {
        String username = SecurityUtils.currentUsername();
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getIsDeleted, 0)
                .last("limit 1"));
        if (user == null) {
            throw new IllegalArgumentException("当前用户不存在");
        }
        return ApiResponse.success(systemService.listMenuTreeByUserId(user.getId()));
    }

    @PostMapping("/menus")
    public ApiResponse<MenuTreeNode> createMenu(@Valid @RequestBody MenuSaveRequest request) {
        return ApiResponse.success(systemService.createMenu(request));
    }

    @PostMapping("/menus/{id}/update")
    public ApiResponse<MenuTreeNode> updateMenu(@PathVariable Long id, @Valid @RequestBody MenuSaveRequest request) {
        return ApiResponse.success(systemService.updateMenu(id, request));
    }

    @PostMapping("/menus/{id}/delete")
    public ApiResponse<Void> deleteMenu(@PathVariable Long id) {
        systemService.deleteMenu(id);
        return ApiResponse.success(null);
    }
}

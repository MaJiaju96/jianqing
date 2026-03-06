package com.jianqing.module.system.controller;

import com.jianqing.common.api.ApiResponse;
import com.jianqing.framework.security.SecurityUtils;
import com.jianqing.module.system.dto.MenuTreeNode;
import com.jianqing.module.system.dto.MenuSaveRequest;
import com.jianqing.module.system.dto.IdListRequest;
import com.jianqing.module.system.dto.RoleSaveRequest;
import com.jianqing.module.system.dto.RoleSummary;
import com.jianqing.module.system.dto.UserSaveRequest;
import com.jianqing.module.system.dto.UserSummary;
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

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    /** 查询用户列表。 */
    @GetMapping("/users")
    public ApiResponse<List<UserSummary>> users() {
        return ApiResponse.success(systemService.listUsers());
    }

    /** 新增用户。 */
    @PostMapping("/users")
    public ApiResponse<UserSummary> createUser(@Valid @RequestBody UserSaveRequest request) {
        return ApiResponse.success(systemService.createUser(request));
    }

    /** 更新用户。 */
    @PostMapping("/users/{id}/update")
    public ApiResponse<UserSummary> updateUser(@PathVariable Long id, @Valid @RequestBody UserSaveRequest request) {
        return ApiResponse.success(systemService.updateUser(id, request));
    }

    /** 删除用户。 */
    @PostMapping("/users/{id}/delete")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        systemService.deleteUser(id);
        return ApiResponse.success(null);
    }

    /** 查询用户已分配角色 ID。 */
    @GetMapping("/users/{id}/role-ids")
    public ApiResponse<List<Long>> userRoleIds(@PathVariable Long id) {
        return ApiResponse.success(systemService.listUserRoleIds(id));
    }

    /** 分配用户角色。 */
    @PostMapping("/users/{id}/roles/assign")
    public ApiResponse<Void> assignUserRoles(@PathVariable Long id, @RequestBody @Valid IdListRequest request) {
        systemService.assignUserRoles(id, request.getIds());
        return ApiResponse.success(null);
    }

    /** 查询角色列表。 */
    @GetMapping("/roles")
    public ApiResponse<List<RoleSummary>> roles() {
        return ApiResponse.success(systemService.listRoles());
    }

    /** 新增角色。 */
    @PostMapping("/roles")
    public ApiResponse<RoleSummary> createRole(@Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.success(systemService.createRole(request));
    }

    /** 更新角色。 */
    @PostMapping("/roles/{id}/update")
    public ApiResponse<RoleSummary> updateRole(@PathVariable Long id, @Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.success(systemService.updateRole(id, request));
    }

    /** 删除角色。 */
    @PostMapping("/roles/{id}/delete")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        systemService.deleteRole(id);
        return ApiResponse.success(null);
    }

    /** 查询角色已分配菜单 ID。 */
    @GetMapping("/roles/{id}/menu-ids")
    public ApiResponse<List<Long>> roleMenuIds(@PathVariable Long id) {
        return ApiResponse.success(systemService.listRoleMenuIds(id));
    }

    /** 分配角色菜单。 */
    @PostMapping("/roles/{id}/menus/assign")
    public ApiResponse<Void> assignRoleMenus(@PathVariable Long id, @RequestBody @Valid IdListRequest request) {
        systemService.assignRoleMenus(id, request.getIds());
        return ApiResponse.success(null);
    }

    /** 查询全量菜单树。 */
    @GetMapping("/menus")
    public ApiResponse<List<MenuTreeNode>> menus() {
        return ApiResponse.success(systemService.listAllMenuTree());
    }

    /** 查询当前用户可见菜单树。 */
    @GetMapping("/menus/tree")
    public ApiResponse<List<MenuTreeNode>> menuTree() {
        String username = SecurityUtils.currentUsername();
        Long userId = systemService.findUserIdByUsername(username);
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("当前用户不存在");
        }
        return ApiResponse.success(systemService.listMenuTreeByUserId(userId));
    }

    /** 新增菜单。 */
    @PostMapping("/menus")
    public ApiResponse<MenuTreeNode> createMenu(@Valid @RequestBody MenuSaveRequest request) {
        return ApiResponse.success(systemService.createMenu(request));
    }

    /** 更新菜单。 */
    @PostMapping("/menus/{id}/update")
    public ApiResponse<MenuTreeNode> updateMenu(@PathVariable Long id, @Valid @RequestBody MenuSaveRequest request) {
        return ApiResponse.success(systemService.updateMenu(id, request));
    }

    /** 删除菜单。 */
    @PostMapping("/menus/{id}/delete")
    public ApiResponse<Void> deleteMenu(@PathVariable Long id) {
        systemService.deleteMenu(id);
        return ApiResponse.success(null);
    }
}

package com.jianqing.module.system.service;

import com.jianqing.module.system.dto.MenuSaveRequest;
import com.jianqing.module.system.dto.MenuTreeNode;
import com.jianqing.module.system.dto.RoleSaveRequest;
import com.jianqing.module.system.dto.RoleSummary;
import com.jianqing.module.system.dto.UserSaveRequest;
import com.jianqing.module.system.dto.UserSummary;

import java.util.List;

public interface SystemService {

    List<UserSummary> listUsers();

    UserSummary createUser(UserSaveRequest request);

    UserSummary updateUser(Long id, UserSaveRequest request);

    void deleteUser(Long id);

    List<Long> listUserRoleIds(Long userId);

    void assignUserRoles(Long userId, List<Long> roleIds);

    List<RoleSummary> listRoles();

    RoleSummary createRole(RoleSaveRequest request);

    RoleSummary updateRole(Long id, RoleSaveRequest request);

    void deleteRole(Long id);

    List<Long> listRoleMenuIds(Long roleId);

    void assignRoleMenus(Long roleId, List<Long> menuIds);

    List<MenuTreeNode> listAllMenuTree();

    MenuTreeNode createMenu(MenuSaveRequest request);

    MenuTreeNode updateMenu(Long id, MenuSaveRequest request);

    void deleteMenu(Long id);

    List<MenuTreeNode> listMenuTreeByUserId(Long userId);

    List<String> listRoleCodesByUserId(Long userId);

    List<String> listPermissionsByUserId(Long userId);
}

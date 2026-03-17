package com.jianqing.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jianqing.module.system.dto.RoleSaveRequest;
import com.jianqing.module.system.dto.RoleSummary;
import com.jianqing.module.system.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口，负责角色维护与角色-菜单关系管理。
 */
public interface RoleService extends IService<SysRole> {

    /**
     * 查询角色列表。
     */
    List<RoleSummary> listRoles();

    /**
     * 新增角色。
     */
    RoleSummary createRole(RoleSaveRequest request);

    /**
     * 更新角色。
     */
    RoleSummary updateRole(Long id, RoleSaveRequest request);

    /**
     * 删除角色。
     */
    void deleteRole(Long id);

    /**
     * 查询角色已分配菜单 ID。
     */
    List<Long> listRoleMenuIds(Long roleId);

    /**
     * 分配角色菜单。
     */
    void assignRoleMenus(Long roleId, List<Long> menuIds);

    /**
     * 查询角色自定义部门 ID。
     */
    List<Long> listRoleCustomDeptIds(Long roleId);

    /**
     * 查询用户角色编码集合。
     */
    List<String> listRoleCodesByUserId(Long userId);

    List<SysRole> listEnabledRolesByUserId(Long userId);

    /**
     * 校验角色 ID 集合是否全部有效。
     */
    void validateRoleIds(List<Long> roleIds);
}

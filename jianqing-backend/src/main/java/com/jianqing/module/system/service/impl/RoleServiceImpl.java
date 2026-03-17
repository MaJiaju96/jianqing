package com.jianqing.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jianqing.module.system.constant.DataScopeConstants;
import com.jianqing.module.system.dto.RoleSaveRequest;
import com.jianqing.module.system.dto.RoleSummary;
import com.jianqing.module.system.entity.SysRole;
import com.jianqing.module.system.mapper.SysRoleMapper;
import com.jianqing.module.system.service.MenuService;
import com.jianqing.module.system.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements RoleService {

    private final MenuService menuService;

    public RoleServiceImpl(MenuService menuService) {
        this.menuService = menuService;
    }

    @Override
    public List<RoleSummary> listRoles() {
        List<SysRole> roles = baseMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getIsDeleted, 0)
                .orderByAsc(SysRole::getId));
        return roles.stream().map(this::toRoleSummary).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleSummary createRole(RoleSaveRequest request) {
        validateRoleCodeUnique(request.getRoleCode(), null);
        validateDataScope(request.getDataScope());
        SysRole role = new SysRole();
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDataScope(request.getDataScope());
        role.setStatus(request.getStatus());
        role.setIsDeleted(0);
        baseMapper.insert(role);
        return toRoleSummary(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleSummary updateRole(Long id, RoleSaveRequest request) {
        SysRole role = getRoleOrThrow(id);
        validateRoleCodeUnique(request.getRoleCode(), id);
        validateDataScope(request.getDataScope());
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDataScope(request.getDataScope());
        role.setStatus(request.getStatus());
        baseMapper.updateById(role);
        return toRoleSummary(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        SysRole role = getRoleOrThrow(id);
        role.setIsDeleted(1);
        baseMapper.updateById(role);
        baseMapper.deleteUserRoleByRoleId(id);
        baseMapper.deleteRoleMenuByRoleId(id);
    }

    @Override
    public List<Long> listRoleMenuIds(Long roleId) {
        getRoleOrThrow(roleId);
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleMenus(Long roleId, List<Long> menuIds) {
        getRoleOrThrow(roleId);
        List<Long> validMenuIds = menuIds == null ? Collections.emptyList() : menuIds.stream().distinct().toList();
        menuService.validateMenuIds(validMenuIds);
        baseMapper.deleteRoleMenuByRoleId(roleId);
        if (!validMenuIds.isEmpty()) {
            baseMapper.batchInsertRoleMenu(roleId, validMenuIds);
        }
    }

    @Override
    public List<String> listRoleCodesByUserId(Long userId) {
        return listEnabledRolesByUserId(userId).stream()
                .map(SysRole::getRoleCode)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<SysRole> listEnabledRolesByUserId(Long userId) {
        return baseMapper.selectEnabledRolesByUserId(userId);
    }

    @Override
    public void validateRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        Long roleCount = baseMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getIsDeleted, 0));
        if (roleCount == null || roleCount != roleIds.size()) {
            throw new IllegalArgumentException("存在无效角色，无法完成分配");
        }
    }

    private RoleSummary toRoleSummary(SysRole role) {
        return new RoleSummary(role.getId(), role.getRoleName(), role.getRoleCode(), role.getDataScope(), role.getStatus());
    }

    private void validateDataScope(Integer dataScope) {
        if (dataScope == null) {
            throw new IllegalArgumentException("数据范围不能为空");
        }
        if (dataScope != DataScopeConstants.ALL
                && dataScope != DataScopeConstants.DEPT_AND_CHILD
                && dataScope != DataScopeConstants.DEPT
                && dataScope != DataScopeConstants.SELF) {
            throw new IllegalArgumentException("当前版本仅支持全部、本部门及以下、本部门、本人四种数据范围");
        }
    }

    private void validateRoleCodeUnique(String roleCode, Long excludeId) {
        Long count = baseMapper.countByRoleCode(roleCode, excludeId);
        if (count != null && count > 0) {
            throw new IllegalArgumentException("角色编码已存在");
        }
    }

    private SysRole getRoleOrThrow(Long id) {
        SysRole role = baseMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, id)
                .eq(SysRole::getIsDeleted, 0)
                .last("limit 1"));
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        return role;
    }
}

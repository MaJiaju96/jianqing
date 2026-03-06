package com.jianqing.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jianqing.framework.cache.CacheConsistencyService;
import com.jianqing.module.system.dto.MenuTreeNode;
import com.jianqing.module.system.dto.MenuSaveRequest;
import com.jianqing.module.system.dto.RoleSaveRequest;
import com.jianqing.module.system.dto.RoleSummary;
import com.jianqing.module.system.dto.UserSaveRequest;
import com.jianqing.module.system.dto.UserSummary;
import com.jianqing.module.system.entity.SysUser;
import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.MenuService;
import com.jianqing.module.system.service.RoleService;
import com.jianqing.module.system.service.SystemService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SystemServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SystemService {

    private static final String CACHE_SYSTEM_USERS = "system:users";
    private static final String CACHE_SYSTEM_ROLES = "system:roles";
    private static final String CACHE_SYSTEM_MENUS = "system:menus";
    private static final String CACHE_USER_MENU_TREE = "user:menu-tree";
    private static final String CACHE_USER_ROLE_CODES = "user:role-codes";
    private static final String CACHE_USER_PERMS = "user:perms";
    private static final String ALL_CACHE_KEY = "all";

    private final RoleService roleService;
    private final MenuService menuService;
    private final PasswordEncoder passwordEncoder;
    private final CacheConsistencyService cacheConsistencyService;

    public SystemServiceImpl(SysUserMapper sysUserMapper,
                             RoleService roleService,
                             MenuService menuService,
                             PasswordEncoder passwordEncoder,
                             CacheConsistencyService cacheConsistencyService) {
        this.baseMapper = sysUserMapper;
        this.roleService = roleService;
        this.menuService = menuService;
        this.passwordEncoder = passwordEncoder;
        this.cacheConsistencyService = cacheConsistencyService;
    }

    @Cacheable(cacheNames = CACHE_SYSTEM_USERS, key = "'" + ALL_CACHE_KEY + "'")
    @Override
    public List<UserSummary> listUsers() {
        List<SysUser> users = baseMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getIsDeleted, 0)
                .orderByAsc(SysUser::getId));
        return users.stream().map(this::toUserSummary).toList();
    }

    @Override
    public UserSummary createUser(UserSaveRequest request) {
        validateUsernameUnique(request.getUsername(), null);
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("新增用户时密码不能为空");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setRealName(safeText(request.getRealName()));
        user.setMobile(safeText(request.getMobile()));
        user.setEmail(safeText(request.getEmail()));
        user.setDeptId(request.getDeptId());
        user.setStatus(request.getStatus());
        user.setIsDeleted(0);
        baseMapper.insert(user);
        evictCache(CACHE_SYSTEM_USERS, ALL_CACHE_KEY);
        return toUserSummary(user);
    }

    @Override
    public UserSummary updateUser(Long id, UserSaveRequest request) {
        SysUser user = getUserOrThrow(id);
        validateUsernameUnique(request.getUsername(), id);

        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setRealName(safeText(request.getRealName()));
        user.setMobile(safeText(request.getMobile()));
        user.setEmail(safeText(request.getEmail()));
        user.setDeptId(request.getDeptId());
        user.setStatus(request.getStatus());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        baseMapper.updateById(user);
        evictCache(CACHE_SYSTEM_USERS, ALL_CACHE_KEY);
        evictUserAuthCache(id);
        return toUserSummary(user);
    }

    @Override
    public void deleteUser(Long id) {
        SysUser user = getUserOrThrow(id);
        user.setIsDeleted(1);
        baseMapper.updateById(user);
        baseMapper.deleteUserRoleByUserId(id);
        evictCache(CACHE_SYSTEM_USERS, ALL_CACHE_KEY);
        evictUserAuthCache(id);
    }

    @Override
    public List<Long> listUserRoleIds(Long userId) {
        getUserOrThrow(userId);
        return baseMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        getUserOrThrow(userId);
        List<Long> validRoleIds = roleIds == null ? Collections.emptyList() : roleIds.stream().distinct().toList();
        roleService.validateRoleIds(validRoleIds);

        baseMapper.deleteUserRoleByUserId(userId);
        if (!validRoleIds.isEmpty()) {
            baseMapper.batchInsertUserRole(userId, validRoleIds);
        }
        evictUserAuthCache(userId);
    }

    @Cacheable(cacheNames = CACHE_SYSTEM_ROLES, key = "'" + ALL_CACHE_KEY + "'")
    @Override
    public List<RoleSummary> listRoles() {
        return roleService.listRoles();
    }

    @Override
    public RoleSummary createRole(RoleSaveRequest request) {
        RoleSummary summary = roleService.createRole(request);
        evictCache(CACHE_SYSTEM_ROLES, ALL_CACHE_KEY);
        return summary;
    }

    @Override
    public RoleSummary updateRole(Long id, RoleSaveRequest request) {
        List<Long> affectedUserIds = baseMapper.selectUserIdsByRoleId(id);
        RoleSummary summary = roleService.updateRole(id, request);
        evictCache(CACHE_SYSTEM_ROLES, ALL_CACHE_KEY);
        evictUserAuthCaches(affectedUserIds);
        return summary;
    }

    @Override
    public void deleteRole(Long id) {
        List<Long> affectedUserIds = baseMapper.selectUserIdsByRoleId(id);
        roleService.deleteRole(id);
        evictCache(CACHE_SYSTEM_ROLES, ALL_CACHE_KEY);
        evictUserAuthCaches(affectedUserIds);
    }

    @Override
    public List<Long> listRoleMenuIds(Long roleId) {
        return roleService.listRoleMenuIds(roleId);
    }

    @Override
    public void assignRoleMenus(Long roleId, List<Long> menuIds) {
        List<Long> affectedUserIds = baseMapper.selectUserIdsByRoleId(roleId);
        // 角色与菜单关系由角色服务维护，当前服务仅负责跨实体缓存一致性收敛。
        roleService.assignRoleMenus(roleId, menuIds);
        evictUserAuthCaches(affectedUserIds);
    }

    @Cacheable(cacheNames = CACHE_SYSTEM_MENUS, key = "'" + ALL_CACHE_KEY + "'")
    @Override
    public List<MenuTreeNode> listAllMenuTree() {
        return menuService.listAllMenuTree();
    }

    @Override
    public MenuTreeNode createMenu(MenuSaveRequest request) {
        MenuTreeNode node = menuService.createMenu(request);
        evictCache(CACHE_SYSTEM_MENUS, ALL_CACHE_KEY);
        return node;
    }

    @Override
    public MenuTreeNode updateMenu(Long id, MenuSaveRequest request) {
        List<Long> affectedUserIds = baseMapper.selectUserIdsByMenuId(id);
        MenuTreeNode node = menuService.updateMenu(id, request);
        evictCache(CACHE_SYSTEM_MENUS, ALL_CACHE_KEY);
        evictUserAuthCaches(affectedUserIds);
        return node;
    }

    @Override
    public void deleteMenu(Long id) {
        List<Long> affectedUserIds = baseMapper.selectUserIdsByMenuId(id);
        menuService.deleteMenu(id);
        evictCache(CACHE_SYSTEM_MENUS, ALL_CACHE_KEY);
        evictUserAuthCaches(affectedUserIds);
    }

    @Cacheable(cacheNames = CACHE_USER_MENU_TREE, key = "#userId")
    @Override
    public List<MenuTreeNode> listMenuTreeByUserId(Long userId) {
        return menuService.listMenuTreeByUserId(userId);
    }

    @Cacheable(cacheNames = CACHE_USER_ROLE_CODES, key = "#userId")
    @Override
    public List<String> listRoleCodesByUserId(Long userId) {
        return roleService.listRoleCodesByUserId(userId);
    }

    @Cacheable(cacheNames = CACHE_USER_PERMS, key = "#userId")
    @Override
    public List<String> listPermissionsByUserId(Long userId) {
        return menuService.listPermissionsByUserId(userId);
    }

    @Override
    public Long findUserIdByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return baseMapper.selectIdByUsername(username);
    }

    private UserSummary toUserSummary(SysUser user) {
        return new UserSummary(user.getId(), user.getUsername(), user.getNickname(), user.getRealName(),
                user.getMobile(), user.getEmail(), user.getDeptId(), user.getStatus());
    }

    private SysUser getUserOrThrow(Long id) {
        SysUser user = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .eq(SysUser::getIsDeleted, 0)
                .last("limit 1"));
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    private void validateUsernameUnique(String username, Long excludeId) {
        Long count = baseMapper.countByUsername(username, excludeId);
        if (count != null && count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void evictUserAuthCache(Long userId) {
        evictCache(CACHE_USER_ROLE_CODES, userId);
        evictCache(CACHE_USER_PERMS, userId);
        evictCache(CACHE_USER_MENU_TREE, userId);
    }

    private void evictUserAuthCaches(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            evictUserAuthCache(userId);
        }
    }

    private void evictCache(String cacheName, Object key) {
        cacheConsistencyService.deleteWithDelay(cacheName, key);
    }
}

package com.jianqing.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianqing.framework.cache.CacheConsistencyService;
import com.jianqing.module.system.dto.MenuTreeNode;
import com.jianqing.module.system.dto.MenuSaveRequest;
import com.jianqing.module.system.dto.RoleSaveRequest;
import com.jianqing.module.system.dto.RoleSummary;
import com.jianqing.module.system.dto.UserSaveRequest;
import com.jianqing.module.system.dto.UserSummary;
import com.jianqing.module.system.entity.SysMenu;
import com.jianqing.module.system.entity.SysRole;
import com.jianqing.module.system.entity.SysUser;
import com.jianqing.module.system.mapper.SysMenuMapper;
import com.jianqing.module.system.mapper.SysRoleMapper;
import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.SystemService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SystemServiceImpl implements SystemService {

    private static final String CACHE_SYSTEM_USERS = "system:users";
    private static final String CACHE_SYSTEM_ROLES = "system:roles";
    private static final String CACHE_SYSTEM_MENUS = "system:menus";
    private static final String CACHE_USER_MENU_TREE = "user:menu-tree";
    private static final String CACHE_USER_ROLE_CODES = "user:role-codes";
    private static final String CACHE_USER_PERMS = "user:perms";
    private static final String ALL_CACHE_KEY = "all";

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final PasswordEncoder passwordEncoder;
    private final CacheConsistencyService cacheConsistencyService;

    public SystemServiceImpl(SysUserMapper sysUserMapper,
                             SysRoleMapper sysRoleMapper,
                             SysMenuMapper sysMenuMapper,
                             PasswordEncoder passwordEncoder,
                             CacheConsistencyService cacheConsistencyService) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.passwordEncoder = passwordEncoder;
        this.cacheConsistencyService = cacheConsistencyService;
    }

    @Cacheable(cacheNames = CACHE_SYSTEM_USERS, key = "'" + ALL_CACHE_KEY + "'")
    @Override
    public List<UserSummary> listUsers() {
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
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
        sysUserMapper.insert(user);
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
        sysUserMapper.updateById(user);
        evictCache(CACHE_SYSTEM_USERS, ALL_CACHE_KEY);
        evictUserAuthCache(id);
        return toUserSummary(user);
    }

    @Override
    public void deleteUser(Long id) {
        SysUser user = getUserOrThrow(id);
        user.setIsDeleted(1);
        sysUserMapper.updateById(user);
        sysUserMapper.deleteUserRoleByUserId(id);
        evictCache(CACHE_SYSTEM_USERS, ALL_CACHE_KEY);
        evictUserAuthCache(id);
    }

    @Override
    public List<Long> listUserRoleIds(Long userId) {
        getUserOrThrow(userId);
        return sysUserMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        getUserOrThrow(userId);
        List<Long> validRoleIds = roleIds == null ? Collections.emptyList() : roleIds.stream().distinct().toList();
        if (!validRoleIds.isEmpty()) {
            Long roleCount = sysRoleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                    .in(SysRole::getId, validRoleIds)
                    .eq(SysRole::getIsDeleted, 0));
            if (roleCount == null || roleCount != validRoleIds.size()) {
                throw new IllegalArgumentException("存在无效角色，无法完成分配");
            }
        }

        sysUserMapper.deleteUserRoleByUserId(userId);
        if (!validRoleIds.isEmpty()) {
            sysUserMapper.batchInsertUserRole(userId, validRoleIds);
        }
        evictUserAuthCache(userId);
    }

    @Cacheable(cacheNames = CACHE_SYSTEM_ROLES, key = "'" + ALL_CACHE_KEY + "'")
    @Override
    public List<RoleSummary> listRoles() {
        List<SysRole> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getIsDeleted, 0)
                .orderByAsc(SysRole::getId));
        return roles.stream().map(this::toRoleSummary).toList();
    }

    @Override
    public RoleSummary createRole(RoleSaveRequest request) {
        validateRoleCodeUnique(request.getRoleCode(), null);
        SysRole role = new SysRole();
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setStatus(request.getStatus());
        role.setIsDeleted(0);
        sysRoleMapper.insert(role);
        evictCache(CACHE_SYSTEM_ROLES, ALL_CACHE_KEY);
        return toRoleSummary(role);
    }

    @Override
    public RoleSummary updateRole(Long id, RoleSaveRequest request) {
        List<Long> affectedUserIds = sysUserMapper.selectUserIdsByRoleId(id);
        SysRole role = getRoleOrThrow(id);
        validateRoleCodeUnique(request.getRoleCode(), id);
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setStatus(request.getStatus());
        sysRoleMapper.updateById(role);
        evictCache(CACHE_SYSTEM_ROLES, ALL_CACHE_KEY);
        evictUserAuthCaches(affectedUserIds);
        return toRoleSummary(role);
    }

    @Override
    public void deleteRole(Long id) {
        List<Long> affectedUserIds = sysUserMapper.selectUserIdsByRoleId(id);
        SysRole role = getRoleOrThrow(id);
        role.setIsDeleted(1);
        sysRoleMapper.updateById(role);
        sysRoleMapper.deleteUserRoleByRoleId(id);
        sysRoleMapper.deleteRoleMenuByRoleId(id);
        evictCache(CACHE_SYSTEM_ROLES, ALL_CACHE_KEY);
        evictUserAuthCaches(affectedUserIds);
    }

    @Override
    public List<Long> listRoleMenuIds(Long roleId) {
        getRoleOrThrow(roleId);
        return sysRoleMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public void assignRoleMenus(Long roleId, List<Long> menuIds) {
        List<Long> affectedUserIds = sysUserMapper.selectUserIdsByRoleId(roleId);
        getRoleOrThrow(roleId);
        List<Long> validMenuIds = menuIds == null ? Collections.emptyList() : menuIds.stream().distinct().toList();
        if (!validMenuIds.isEmpty()) {
            Long menuCount = sysMenuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                    .in(SysMenu::getId, validMenuIds)
                    .eq(SysMenu::getIsDeleted, 0));
            if (menuCount == null || menuCount != validMenuIds.size()) {
                throw new IllegalArgumentException("存在无效菜单，无法完成分配");
            }
        }

        sysRoleMapper.deleteRoleMenuByRoleId(roleId);
        if (!validMenuIds.isEmpty()) {
            sysRoleMapper.batchInsertRoleMenu(roleId, validMenuIds);
        }
        evictUserAuthCaches(affectedUserIds);
    }

    @Cacheable(cacheNames = CACHE_SYSTEM_MENUS, key = "'" + ALL_CACHE_KEY + "'")
    @Override
    public List<MenuTreeNode> listAllMenuTree() {
        return buildMenuTree(sysMenuMapper.selectAllEnabledMenus());
    }

    @Override
    public MenuTreeNode createMenu(MenuSaveRequest request) {
        validateMenuParent(request.getParentId());
        SysMenu menu = new SysMenu();
        fillMenu(menu, request);
        menu.setIsDeleted(0);
        sysMenuMapper.insert(menu);
        evictCache(CACHE_SYSTEM_MENUS, ALL_CACHE_KEY);
        return toMenuTreeNode(menu);
    }

    @Override
    public MenuTreeNode updateMenu(Long id, MenuSaveRequest request) {
        List<Long> affectedUserIds = sysUserMapper.selectUserIdsByMenuId(id);
        SysMenu menu = getMenuOrThrow(id);
        validateMenuParent(request.getParentId());
        if (id.equals(request.getParentId())) {
            throw new IllegalArgumentException("菜单不能将自己设置为父节点");
        }
        fillMenu(menu, request);
        sysMenuMapper.updateById(menu);
        evictCache(CACHE_SYSTEM_MENUS, ALL_CACHE_KEY);
        evictUserAuthCaches(affectedUserIds);
        return toMenuTreeNode(menu);
    }

    @Override
    public void deleteMenu(Long id) {
        List<Long> affectedUserIds = sysUserMapper.selectUserIdsByMenuId(id);
        SysMenu menu = getMenuOrThrow(id);
        Long childCount = sysMenuMapper.countChildren(id);
        if (childCount != null && childCount > 0) {
            throw new IllegalArgumentException("请先删除子菜单后再删除当前菜单");
        }
        menu.setIsDeleted(1);
        sysMenuMapper.updateById(menu);
        sysMenuMapper.deleteRoleMenuByMenuId(id);
        evictCache(CACHE_SYSTEM_MENUS, ALL_CACHE_KEY);
        evictUserAuthCaches(affectedUserIds);
    }

    @Cacheable(cacheNames = CACHE_USER_MENU_TREE, key = "#userId")
    @Override
    public List<MenuTreeNode> listMenuTreeByUserId(Long userId) {
        return buildMenuTree(sysMenuMapper.selectEnabledMenusByUserId(userId));
    }

    private List<MenuTreeNode> buildMenuTree(List<SysMenu> menus) {
        Map<Long, MenuTreeNode> nodeMap = new HashMap<>();
        for (SysMenu menu : menus) {
            MenuTreeNode node = toMenuTreeNode(menu);
            nodeMap.put(node.getId(), node);
        }

        List<MenuTreeNode> roots = new ArrayList<>();
        for (MenuTreeNode node : nodeMap.values()) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0 || !nodeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                nodeMap.get(parentId).getChildren().add(node);
            }
        }
        sortTree(roots);
        return roots;
    }

    @Cacheable(cacheNames = CACHE_USER_ROLE_CODES, key = "#userId")
    @Override
    public List<String> listRoleCodesByUserId(Long userId) {
        return sysRoleMapper.selectEnabledRolesByUserId(userId).stream()
                .map(SysRole::getRoleCode)
                .filter(Objects::nonNull)
                .toList();
    }

    @Cacheable(cacheNames = CACHE_USER_PERMS, key = "#userId")
    @Override
    public List<String> listPermissionsByUserId(Long userId) {
        return sysMenuMapper.selectPermMenusByUserId(userId).stream()
                .map(SysMenu::getPerms)
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    private void sortTree(List<MenuTreeNode> nodes) {
        nodes.sort(Comparator.comparing(MenuTreeNode::getSortNo, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuTreeNode::getId, Comparator.nullsLast(Long::compareTo)));
        for (MenuTreeNode node : nodes) {
            sortTree(node.getChildren());
        }
    }

    private UserSummary toUserSummary(SysUser user) {
        return new UserSummary(user.getId(), user.getUsername(), user.getNickname(), user.getRealName(),
                user.getMobile(), user.getEmail(), user.getDeptId(), user.getStatus());
    }

    private RoleSummary toRoleSummary(SysRole role) {
        return new RoleSummary(role.getId(), role.getRoleName(), role.getRoleCode(), role.getStatus());
    }

    private MenuTreeNode toMenuTreeNode(SysMenu menu) {
        MenuTreeNode node = new MenuTreeNode();
        node.setId(menu.getId());
        node.setParentId(menu.getParentId());
        node.setMenuType(menu.getMenuType());
        node.setMenuName(menu.getMenuName());
        node.setRoutePath(menu.getRoutePath());
        node.setComponent(menu.getComponent());
        node.setPerms(menu.getPerms());
        node.setIcon(menu.getIcon());
        node.setSortNo(menu.getSortNo());
        return node;
    }

    private SysUser getUserOrThrow(Long id) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .eq(SysUser::getIsDeleted, 0)
                .last("limit 1"));
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    private SysRole getRoleOrThrow(Long id) {
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, id)
                .eq(SysRole::getIsDeleted, 0)
                .last("limit 1"));
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        return role;
    }

    private SysMenu getMenuOrThrow(Long id) {
        SysMenu menu = sysMenuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getId, id)
                .eq(SysMenu::getIsDeleted, 0)
                .last("limit 1"));
        if (menu == null) {
            throw new IllegalArgumentException("菜单不存在");
        }
        return menu;
    }

    private void validateUsernameUnique(String username, Long excludeId) {
        Long count = sysUserMapper.countByUsername(username, excludeId);
        if (count != null && count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
    }

    private void validateRoleCodeUnique(String roleCode, Long excludeId) {
        Long count = sysRoleMapper.countByRoleCode(roleCode, excludeId);
        if (count != null && count > 0) {
            throw new IllegalArgumentException("角色编码已存在");
        }
    }

    private void validateMenuParent(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        SysMenu parentMenu = sysMenuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getId, parentId)
                .eq(SysMenu::getIsDeleted, 0)
                .last("limit 1"));
        if (parentMenu == null) {
            throw new IllegalArgumentException("父菜单不存在");
        }
    }

    private void fillMenu(SysMenu menu, MenuSaveRequest request) {
        menu.setParentId(request.getParentId());
        menu.setMenuType(request.getMenuType());
        menu.setMenuName(request.getMenuName());
        menu.setRoutePath(safeText(request.getRoutePath()));
        menu.setComponent(safeText(request.getComponent()));
        menu.setPerms(safeText(request.getPerms()));
        menu.setIcon(safeText(request.getIcon()));
        menu.setSortNo(request.getSortNo());
        menu.setVisible(request.getVisible());
        menu.setStatus(request.getStatus());
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

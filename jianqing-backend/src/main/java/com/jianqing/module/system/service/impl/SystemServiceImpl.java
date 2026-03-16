package com.jianqing.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jianqing.module.system.dto.DeptSaveRequest;
import com.jianqing.module.system.dto.DeptTreeNode;
import com.jianqing.module.system.dto.ConfigSaveRequest;
import com.jianqing.module.system.dto.ConfigHistorySummary;
import com.jianqing.module.system.dto.ConfigDiffSummary;
import com.jianqing.module.system.dto.ConfigRestorePreviewSummary;
import com.jianqing.module.system.dto.ConfigSummary;
import com.jianqing.module.system.dto.DictDataSaveRequest;
import com.jianqing.module.system.dto.DictDataSummary;
import com.jianqing.module.system.dto.DictOptionItem;
import com.jianqing.module.system.dto.DictTypeSaveRequest;
import com.jianqing.module.system.dto.DictTypeSummary;
import com.jianqing.module.system.dto.MenuTreeNode;
import com.jianqing.module.system.dto.MenuSaveRequest;
import com.jianqing.module.system.dto.RoleSaveRequest;
import com.jianqing.module.system.dto.RoleSummary;
import com.jianqing.module.system.dto.UserSaveRequest;
import com.jianqing.module.system.dto.UserSummary;
import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.DictDataService;
import com.jianqing.module.system.service.DictTypeService;
import com.jianqing.module.system.service.ConfigService;
import com.jianqing.module.system.service.DeptService;
import com.jianqing.module.system.service.MenuService;
import com.jianqing.module.system.service.RoleService;
import com.jianqing.module.system.service.SystemService;
import com.jianqing.module.system.entity.SysUser;
import com.jianqing.module.system.service.impl.UserDataScopeResolver.CurrentDataScope;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SystemService {

    private static final String CACHE_SYSTEM_USERS = "system:users";
    private static final String CACHE_SYSTEM_ROLES = "system:roles";
    private static final String CACHE_SYSTEM_MENUS = "system:menus";
    private static final String CACHE_SYSTEM_DICT_TYPES = "system:dict-types";
    private static final String CACHE_SYSTEM_DICT_DATA = "system:dict-data";
    private static final String CACHE_SYSTEM_DICT_OPTIONS = "system:dict-options";
    private static final String CACHE_SYSTEM_CONFIGS = "system:configs";
    private static final String CACHE_USER_MENU_TREE = "user:menu-tree";
    private static final String CACHE_USER_ROLE_CODES = "user:role-codes";
    private static final String CACHE_USER_PERMS = "user:perms";
    private static final String ALL_CACHE_KEY = "all";

    private final RoleService roleService;
    private final DeptService deptService;
    private final DictTypeService dictTypeService;
    private final DictDataService dictDataService;
    private final ConfigService configService;
    private final MenuService menuService;
    private final UserDataScopeResolver userDataScopeResolver;
    private final UserWriteOperationHandler userWriteOperationHandler;
    private final UserRoleAssignmentHandler userRoleAssignmentHandler;
    private final RoleMenuAssignmentHandler roleMenuAssignmentHandler;
    private final RoleWriteOperationHandler roleWriteOperationHandler;
    private final MenuWriteOperationHandler menuWriteOperationHandler;
    private final DeptWriteOperationHandler deptWriteOperationHandler;

    public SystemServiceImpl(SysUserMapper sysUserMapper,
                             RoleService roleService,
                             DeptService deptService,
                             DictTypeService dictTypeService,
                             DictDataService dictDataService,
                             ConfigService configService,
                             MenuService menuService,
                             UserDataScopeResolver userDataScopeResolver,
                             UserWriteOperationHandler userWriteOperationHandler,
                             UserRoleAssignmentHandler userRoleAssignmentHandler,
                             RoleMenuAssignmentHandler roleMenuAssignmentHandler,
                             RoleWriteOperationHandler roleWriteOperationHandler,
                             MenuWriteOperationHandler menuWriteOperationHandler,
                             DeptWriteOperationHandler deptWriteOperationHandler) {
        this.baseMapper = sysUserMapper;
        this.roleService = roleService;
        this.deptService = deptService;
        this.dictTypeService = dictTypeService;
        this.dictDataService = dictDataService;
        this.configService = configService;
        this.menuService = menuService;
        this.userDataScopeResolver = userDataScopeResolver;
        this.userWriteOperationHandler = userWriteOperationHandler;
        this.userRoleAssignmentHandler = userRoleAssignmentHandler;
        this.roleMenuAssignmentHandler = roleMenuAssignmentHandler;
        this.roleWriteOperationHandler = roleWriteOperationHandler;
        this.menuWriteOperationHandler = menuWriteOperationHandler;
        this.deptWriteOperationHandler = deptWriteOperationHandler;
    }

    @Cacheable(cacheNames = CACHE_SYSTEM_USERS, key = "'" + ALL_CACHE_KEY + "'")
    @Override
    public List<UserSummary> listUsers() {
        CurrentDataScope currentDataScope = userDataScopeResolver.resolveCurrentDataScope();
        List<SysUser> users = baseMapper.selectList(userDataScopeResolver.buildUserDataScopeQuery(currentDataScope)
                .orderByAsc(SysUser::getId));
        return users.stream().map(this::toUserSummary).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserSummary createUser(UserSaveRequest request) {
        CurrentDataScope currentDataScope = userDataScopeResolver.resolveCurrentDataScope();
        userDataScopeResolver.validateUserCreatePermission(request, currentDataScope);
        validateUsernameUnique(request.getUsername(), null);
        SysUser user = userWriteOperationHandler.createUser(request);
        return toUserSummary(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserSummary updateUser(Long id, UserSaveRequest request) {
        CurrentDataScope currentDataScope = userDataScopeResolver.resolveCurrentDataScope();
        SysUser user = getAccessibleUserOrThrow(id, currentDataScope);
        userDataScopeResolver.validateUserUpdatePermission(request, currentDataScope);
        validateUsernameUnique(request.getUsername(), id);
        userWriteOperationHandler.updateUser(user, request);
        return toUserSummary(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        SysUser user = getAccessibleUserOrThrow(id, userDataScopeResolver.resolveCurrentDataScope());
        userWriteOperationHandler.deleteUser(user);
    }

    @Override
    public List<Long> listUserRoleIds(Long userId) {
        getAccessibleUserOrThrow(userId, userDataScopeResolver.resolveCurrentDataScope());
        return baseMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        getAccessibleUserOrThrow(userId, userDataScopeResolver.resolveCurrentDataScope());
        userRoleAssignmentHandler.assignUserRoles(userId, roleIds);
    }

    @Cacheable(cacheNames = CACHE_SYSTEM_ROLES, key = "'" + ALL_CACHE_KEY + "'")
    @Override
    public List<RoleSummary> listRoles() {
        return roleService.listRoles();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleSummary createRole(RoleSaveRequest request) {
        return roleWriteOperationHandler.createRole(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleSummary updateRole(Long id, RoleSaveRequest request) {
        return roleWriteOperationHandler.updateRole(id, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        roleWriteOperationHandler.deleteRole(id);
    }

    @Override
    public List<Long> listRoleMenuIds(Long roleId) {
        return roleService.listRoleMenuIds(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuAssignmentHandler.assignRoleMenus(roleId, menuIds);
    }

    @Override
    public List<DeptTreeNode> listDeptTree() {
        return deptService.listDeptTree();
    }

    @Override
    public DeptTreeNode createDept(DeptSaveRequest request) {
        return deptWriteOperationHandler.createDept(request);
    }

    @Override
    public DeptTreeNode updateDept(Long id, DeptSaveRequest request) {
        return deptWriteOperationHandler.updateDept(id, request);
    }

    @Override
    public void deleteDept(Long id) {
        deptWriteOperationHandler.deleteDept(id);
    }

    @Cacheable(cacheNames = CACHE_SYSTEM_DICT_TYPES, key = "'" + ALL_CACHE_KEY + "'")
    @Override
    public List<DictTypeSummary> listDictTypes() {
        return dictTypeService.listDictTypes();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictTypeSummary createDictType(DictTypeSaveRequest request) {
        return dictTypeService.createDictType(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictTypeSummary updateDictType(Long id, DictTypeSaveRequest request) {
        return dictTypeService.updateDictType(id, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictType(Long id) {
        dictTypeService.deleteDictType(id);
    }

    @Cacheable(cacheNames = CACHE_SYSTEM_DICT_DATA, key = "#dictType == null ? '' : #dictType.trim()")
    @Override
    public List<DictDataSummary> listDictData(String dictType) {
        return dictDataService.listDictData(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictDataSummary createDictData(DictDataSaveRequest request) {
        return dictDataService.createDictData(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictDataSummary updateDictData(Long id, DictDataSaveRequest request) {
        return dictDataService.updateDictData(id, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictData(Long id) {
        dictDataService.deleteDictData(id);
    }

    @Cacheable(cacheNames = CACHE_SYSTEM_DICT_OPTIONS, key = "#dictType == null ? '' : #dictType.trim()")
    @Override
    public List<DictOptionItem> listDictOptions(String dictType) {
        return dictDataService.listEnabledOptions(dictType);
    }

    @Cacheable(cacheNames = CACHE_SYSTEM_CONFIGS, key = "'" + ALL_CACHE_KEY + "'")
    @Override
    public List<ConfigSummary> listConfigs() {
        return configService.listConfigs();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigSummary createConfig(ConfigSaveRequest request) {
        return configService.createConfig(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigSummary updateConfig(Long id, ConfigSaveRequest request) {
        return configService.updateConfig(id, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        configService.deleteConfig(id);
    }

    @Override
    public List<ConfigHistorySummary> listConfigHistory(Long configId) {
        return configService.listConfigHistory(configId);
    }

    @Override
    public List<ConfigHistorySummary> listDeletedConfigHistory() {
        return configService.listDeletedConfigHistory();
    }

    @Override
    public ConfigRestorePreviewSummary previewDeletedConfigRestore(Long historyId) {
        return configService.previewDeletedConfigRestore(historyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigSummary rollbackConfig(Long configId, Long historyId) {
        return configService.rollbackConfig(configId, historyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigSummary restoreDeletedConfig(Long historyId) {
        return configService.restoreDeletedConfig(historyId);
    }

    @Override
    public ConfigDiffSummary diffConfig(Long configId, Long historyId, Long compareHistoryId) {
        return configService.diffConfig(configId, historyId, compareHistoryId);
    }

    @Cacheable(cacheNames = CACHE_SYSTEM_MENUS, key = "'" + ALL_CACHE_KEY + "'")
    @Override
    public List<MenuTreeNode> listAllMenuTree() {
        return menuService.listAllMenuTree();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuTreeNode createMenu(MenuSaveRequest request) {
        return menuWriteOperationHandler.createMenu(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuTreeNode updateMenu(Long id, MenuSaveRequest request) {
        return menuWriteOperationHandler.updateMenu(id, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        menuWriteOperationHandler.deleteMenu(id);
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

    private SysUser getAccessibleUserOrThrow(Long id, CurrentDataScope currentDataScope) {
        SysUser user = getUserOrThrow(id);
        if (!userDataScopeResolver.canAccessUser(user, currentDataScope)) {
            throw new IllegalArgumentException("当前数据范围不允许访问该用户");
        }
        return user;
    }

    private void validateUsernameUnique(String username, Long excludeId) {
        Long count = baseMapper.countByUsername(username, excludeId);
        if (count != null && count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
    }
}

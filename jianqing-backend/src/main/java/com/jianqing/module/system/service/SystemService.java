package com.jianqing.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jianqing.module.system.dto.MenuSaveRequest;
import com.jianqing.module.system.dto.MenuTreeNode;
import com.jianqing.module.system.dto.MyNoticeDetailSummary;
import com.jianqing.module.system.dto.MyNoticeSummary;
import com.jianqing.module.system.dto.NoticeDetailSummary;
import com.jianqing.module.system.dto.NoticeSaveRequest;
import com.jianqing.module.system.dto.NoticeSummary;
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
import com.jianqing.module.system.dto.RoleSaveRequest;
import com.jianqing.module.system.dto.RoleSummary;
import com.jianqing.module.system.dto.UserSaveRequest;
import com.jianqing.module.system.dto.UserSummary;
import com.jianqing.module.system.entity.SysUser;

import java.util.List;

/**
 * 系统服务聚合接口，负责用户侧编排与跨角色/菜单缓存一致性收敛。
 */
public interface SystemService extends IService<SysUser> {

    /**
     * 查询用户列表。
     */
    List<UserSummary> listUsers();

    /**
     * 新增用户。
     */
    UserSummary createUser(UserSaveRequest request);

    /**
     * 更新用户。
     */
    UserSummary updateUser(Long id, UserSaveRequest request);

    /**
     * 删除用户。
     */
    void deleteUser(Long id);

    /**
     * 查询用户已分配角色 ID。
     */
    List<Long> listUserRoleIds(Long userId);

    /**
     * 分配用户角色。
     */
    void assignUserRoles(Long userId, List<Long> roleIds);

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

    List<DeptTreeNode> listDeptTree();

    DeptTreeNode createDept(DeptSaveRequest request);

    DeptTreeNode updateDept(Long id, DeptSaveRequest request);

    void deleteDept(Long id);

    List<DictTypeSummary> listDictTypes();

    DictTypeSummary createDictType(DictTypeSaveRequest request);

    DictTypeSummary updateDictType(Long id, DictTypeSaveRequest request);

    void deleteDictType(Long id);

    List<DictDataSummary> listDictData(String dictType);

    DictDataSummary createDictData(DictDataSaveRequest request);

    DictDataSummary updateDictData(Long id, DictDataSaveRequest request);

    void deleteDictData(Long id);

    List<DictOptionItem> listDictOptions(String dictType);

    List<ConfigSummary> listConfigs();

    ConfigSummary createConfig(ConfigSaveRequest request);

    ConfigSummary updateConfig(Long id, ConfigSaveRequest request);

    void deleteConfig(Long id);

    List<ConfigHistorySummary> listConfigHistory(Long configId);

    List<ConfigHistorySummary> listDeletedConfigHistory();

    ConfigRestorePreviewSummary previewDeletedConfigRestore(Long historyId);

    ConfigSummary rollbackConfig(Long configId, Long historyId);

    ConfigSummary restoreDeletedConfig(Long historyId);

    ConfigDiffSummary diffConfig(Long configId, Long historyId, Long compareHistoryId);

    List<NoticeSummary> listNotices();

    NoticeDetailSummary getNoticeDetail(Long id);

    NoticeDetailSummary createNotice(NoticeSaveRequest request);

    NoticeDetailSummary updateNotice(Long id, NoticeSaveRequest request);

    NoticeDetailSummary publishNotice(Long id);

    NoticeDetailSummary cancelNotice(Long id);

    void deleteNotice(Long id);

    List<MyNoticeSummary> listMyNotices(Long userId);

    MyNoticeDetailSummary getMyNoticeDetail(Long userId, Long noticeId);

    Long countUnreadNotices(Long userId);

    List<MyNoticeSummary> listLatestNotices(Long userId, int limit);

    List<MyNoticeSummary> listPopupCandidates(Long userId, int limit);

    void markNoticeRead(Long userId, Long noticeId);

    void markAllNoticesRead(Long userId);

    /**
     * 查询全量菜单树。
     */
    List<MenuTreeNode> listAllMenuTree();

    /**
     * 新增菜单。
     */
    MenuTreeNode createMenu(MenuSaveRequest request);

    /**
     * 更新菜单。
     */
    MenuTreeNode updateMenu(Long id, MenuSaveRequest request);

    /**
     * 删除菜单。
     */
    void deleteMenu(Long id);

    /**
     * 查询用户可见菜单树。
     */
    List<MenuTreeNode> listMenuTreeByUserId(Long userId);

    /**
     * 查询用户角色编码。
     */
    List<String> listRoleCodesByUserId(Long userId);

    /**
     * 查询用户权限串。
     */
    List<String> listPermissionsByUserId(Long userId);

    /**
     * 按用户名查询用户 ID。
     */
    Long findUserIdByUsername(String username);
}

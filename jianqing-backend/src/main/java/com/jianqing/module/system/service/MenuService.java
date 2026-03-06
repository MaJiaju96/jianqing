package com.jianqing.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jianqing.module.system.dto.MenuSaveRequest;
import com.jianqing.module.system.dto.MenuTreeNode;
import com.jianqing.module.system.entity.SysMenu;

import java.util.List;

/**
 * 菜单服务接口，负责菜单树查询、菜单维护与权限串查询。
 */
public interface MenuService extends IService<SysMenu> {

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
     * 查询指定用户可见菜单树。
     */
    List<MenuTreeNode> listMenuTreeByUserId(Long userId);

    /**
     * 查询指定用户权限串集合。
     */
    List<String> listPermissionsByUserId(Long userId);

    /**
     * 校验菜单 ID 集合是否全部有效。
     */
    void validateMenuIds(List<Long> menuIds);
}

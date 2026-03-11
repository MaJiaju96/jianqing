package com.jianqing.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jianqing.module.system.dto.MenuSaveRequest;
import com.jianqing.module.system.dto.MenuTreeNode;
import com.jianqing.module.system.entity.SysMenu;
import com.jianqing.module.system.mapper.SysMenuMapper;
import com.jianqing.module.system.service.MenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 菜单服务：增删改查、树形构建、权限过滤
 * 菜单树采用 parentId 关联，删除时级联清除角色-菜单关联
 */
@Service
public class MenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements MenuService {

    @Override
    public List<MenuTreeNode> listAllMenuTree() {
        return buildMenuTree(baseMapper.selectAllEnabledMenus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuTreeNode createMenu(MenuSaveRequest request) {
        validateMenuParent(request.getParentId());
        SysMenu menu = new SysMenu();
        fillMenu(menu, request);
        menu.setIsDeleted(0);
        baseMapper.insert(menu);
        return toMenuTreeNode(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuTreeNode updateMenu(Long id, MenuSaveRequest request) {
        SysMenu menu = getMenuOrThrow(id);
        validateMenuParent(request.getParentId());
        if (id.equals(request.getParentId())) {
            throw new IllegalArgumentException("菜单不能将自己设置为父节点");
        }
        fillMenu(menu, request);
        baseMapper.updateById(menu);
        return toMenuTreeNode(menu);
    }

    /**
     * 删除菜单：逻辑删除 + 校验子菜单 + 级联清除角色关联
     * @param id 菜单ID
     * @throws IllegalArgumentException 菜单不存在或存在子菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        SysMenu menu = getMenuOrThrow(id);
        Long childCount = baseMapper.countChildren(id);
        if (childCount != null && childCount > 0) {
            throw new IllegalArgumentException("请先删除子菜单后再删除当前菜单");
        }
        menu.setIsDeleted(1);
        baseMapper.updateById(menu);
        baseMapper.deleteRoleMenuByMenuId(id);
    }

    @Override
    public List<MenuTreeNode> listMenuTreeByUserId(Long userId) {
        return buildMenuTree(baseMapper.selectEnabledMenusByUserId(userId));
    }

    @Override
    public List<String> listPermissionsByUserId(Long userId) {
        return baseMapper.selectPermMenusByUserId(userId).stream()
                .map(SysMenu::getPerms)
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void validateMenuIds(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        Long menuCount = baseMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getIsDeleted, 0));
        if (menuCount == null || menuCount != menuIds.size()) {
            throw new IllegalArgumentException("存在无效菜单，无法完成分配");
        }
    }

    private void validateMenuParent(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        SysMenu parentMenu = baseMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getId, parentId)
                .eq(SysMenu::getIsDeleted, 0)
                .last("limit 1"));
        if (parentMenu == null) {
            throw new IllegalArgumentException("父菜单不存在");
        }
    }

    private SysMenu getMenuOrThrow(Long id) {
        SysMenu menu = baseMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getId, id)
                .eq(SysMenu::getIsDeleted, 0)
                .last("limit 1"));
        if (menu == null) {
            throw new IllegalArgumentException("菜单不存在");
        }
        return menu;
    }

    /**
     * 构建菜单树：先将所有菜单转为节点Map，再按parentId挂载到父节点
     * @param menus 数据库查询出的菜单列表
     * @return 树形结构（根节点parentId为0或null）
     */
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

    private void sortTree(List<MenuTreeNode> nodes) {
        nodes.sort(Comparator.comparing(MenuTreeNode::getSortNo, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuTreeNode::getId, Comparator.nullsLast(Long::compareTo)));
        for (MenuTreeNode node : nodes) {
            sortTree(node.getChildren());
        }
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
}

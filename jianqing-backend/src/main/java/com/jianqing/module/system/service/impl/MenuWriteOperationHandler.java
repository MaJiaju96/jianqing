package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.dto.MenuSaveRequest;
import com.jianqing.module.system.dto.MenuTreeNode;
import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.MenuService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MenuWriteOperationHandler {

    private final SysUserMapper sysUserMapper;
    private final MenuService menuService;
    private final SystemCacheEvictor systemCacheEvictor;

    public MenuWriteOperationHandler(SysUserMapper sysUserMapper,
                                     MenuService menuService,
                                     SystemCacheEvictor systemCacheEvictor) {
        this.sysUserMapper = sysUserMapper;
        this.menuService = menuService;
        this.systemCacheEvictor = systemCacheEvictor;
    }

    public MenuTreeNode createMenu(MenuSaveRequest request) {
        MenuTreeNode node = menuService.createMenu(request);
        systemCacheEvictor.evictSystemMenus();
        return node;
    }

    public MenuTreeNode updateMenu(Long id, MenuSaveRequest request) {
        List<Long> affectedUserIds = sysUserMapper.selectUserIdsByMenuId(id);
        MenuTreeNode node = menuService.updateMenu(id, request);
        systemCacheEvictor.evictSystemMenus();
        systemCacheEvictor.evictUserAuthBatch(affectedUserIds);
        return node;
    }

    public void deleteMenu(Long id) {
        List<Long> affectedUserIds = sysUserMapper.selectUserIdsByMenuId(id);
        menuService.deleteMenu(id);
        systemCacheEvictor.evictSystemMenus();
        systemCacheEvictor.evictUserAuthBatch(affectedUserIds);
    }
}

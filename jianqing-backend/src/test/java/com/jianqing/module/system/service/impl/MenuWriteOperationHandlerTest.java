package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.dto.MenuSaveRequest;
import com.jianqing.module.system.dto.MenuTreeNode;
import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.MenuService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuWriteOperationHandlerTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private MenuService menuService;

    @Mock
    private SystemCacheEvictor systemCacheEvictor;

    @Test
    void shouldCreateMenuAndEvictSystemMenus() {
        MenuWriteOperationHandler handler = new MenuWriteOperationHandler(sysUserMapper, menuService, systemCacheEvictor);
        MenuSaveRequest request = new MenuSaveRequest();
        MenuTreeNode node = new MenuTreeNode();
        node.setId(1L);
        when(menuService.createMenu(request)).thenReturn(node);

        MenuTreeNode actual = handler.createMenu(request);

        verify(menuService).createMenu(request);
        verify(systemCacheEvictor).evictSystemMenus();
        Assertions.assertSame(node, actual);
    }

    @Test
    void shouldUpdateMenuAndEvictAffectedUserAuth() {
        MenuWriteOperationHandler handler = new MenuWriteOperationHandler(sysUserMapper, menuService, systemCacheEvictor);
        MenuSaveRequest request = new MenuSaveRequest();
        MenuTreeNode node = new MenuTreeNode();
        node.setId(2L);
        when(sysUserMapper.selectUserIdsByMenuId(2L)).thenReturn(Arrays.asList(5L, 6L));
        when(menuService.updateMenu(2L, request)).thenReturn(node);

        MenuTreeNode actual = handler.updateMenu(2L, request);

        verify(menuService).updateMenu(2L, request);
        verify(systemCacheEvictor).evictSystemMenus();
        verify(systemCacheEvictor).evictUserAuthBatch(Arrays.asList(5L, 6L));
        Assertions.assertSame(node, actual);
    }

    @Test
    void shouldDeleteMenuAndEvictAffectedUserAuth() {
        MenuWriteOperationHandler handler = new MenuWriteOperationHandler(sysUserMapper, menuService, systemCacheEvictor);
        when(sysUserMapper.selectUserIdsByMenuId(3L)).thenReturn(Arrays.asList(7L, 8L));

        handler.deleteMenu(3L);

        verify(menuService).deleteMenu(3L);
        verify(systemCacheEvictor).evictSystemMenus();
        verify(systemCacheEvictor).evictUserAuthBatch(Arrays.asList(7L, 8L));
    }
}

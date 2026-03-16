package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.dto.DictTypeSaveRequest;
import com.jianqing.module.system.dto.DictTypeSummary;
import com.jianqing.module.system.entity.SysDictType;
import com.jianqing.module.system.mapper.SysDictDataMapper;
import com.jianqing.module.system.mapper.SysDictTypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictTypeServiceImplTest {

    @Mock
    private SysDictTypeMapper sysDictTypeMapper;

    @Mock
    private SysDictDataMapper sysDictDataMapper;

    @Mock
    private SystemCacheEvictor systemCacheEvictor;

    private DictTypeServiceImpl dictTypeService;

    @BeforeEach
    void setUp() {
        dictTypeService = new DictTypeServiceImpl(sysDictTypeMapper, sysDictDataMapper, systemCacheEvictor);
    }

    @Test
    void shouldListDictTypes() {
        SysDictType entity = new SysDictType();
        entity.setId(1L);
        entity.setDictName("用户状态");
        entity.setDictType("sys_user_status");
        entity.setStatus(1);
        entity.setRemark("测试");
        when(sysDictTypeMapper.selectAllDictTypes()).thenReturn(List.of(entity));

        List<DictTypeSummary> result = dictTypeService.listDictTypes();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("sys_user_status", result.get(0).dictType());
    }

    @Test
    void shouldCreateDictType() {
        when(sysDictTypeMapper.countByDictType("sys_user_status", null)).thenReturn(0L);
        when(sysDictTypeMapper.insert(any(SysDictType.class))).thenAnswer(invocation -> {
            SysDictType entity = invocation.getArgument(0);
            entity.setId(11L);
            return 1;
        });

        DictTypeSummary result = dictTypeService.createDictType(buildRequest());

        Assertions.assertEquals(11L, result.id());
        Assertions.assertEquals("sys_user_status", result.dictType());
        ArgumentCaptor<SysDictType> captor = ArgumentCaptor.forClass(SysDictType.class);
        verify(sysDictTypeMapper).insert(captor.capture());
        Assertions.assertEquals("用户状态", captor.getValue().getDictName());
        verify(systemCacheEvictor).evictSystemDictTypes();
    }

    @Test
    void shouldSyncDictDataWhenDictTypeChanged() {
        SysDictType entity = new SysDictType();
        entity.setId(1L);
        entity.setDictName("用户状态");
        entity.setDictType("sys_user_status");
        when(sysDictTypeMapper.selectById(1L)).thenReturn(entity);
        when(sysDictTypeMapper.countByDictType("sys_account_status", 1L)).thenReturn(0L);

        DictTypeSaveRequest request = buildRequest();
        request.setDictType("sys_account_status");
        DictTypeSummary result = dictTypeService.updateDictType(1L, request);

        Assertions.assertEquals("sys_account_status", result.dictType());
        verify(systemCacheEvictor).evictSystemDictTypes();
        verify(systemCacheEvictor).evictSystemDictData("sys_user_status");
        verify(systemCacheEvictor).evictSystemDictData("sys_account_status");
        verify(sysDictDataMapper).updateDictType("sys_user_status", "sys_account_status");
    }

    @Test
    void shouldRejectDeleteWhenDictDataExists() {
        SysDictType entity = new SysDictType();
        entity.setId(1L);
        entity.setDictType("sys_user_status");
        when(sysDictTypeMapper.selectById(1L)).thenReturn(entity);
        when(sysDictDataMapper.countByDictType("sys_user_status")).thenReturn(1L);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> dictTypeService.deleteDictType(1L));

        Assertions.assertEquals("当前字典类型下仍存在字典数据，无法删除", exception.getMessage());
    }

    @Test
    void shouldEvictCacheWhenDeleteDictType() {
        SysDictType entity = new SysDictType();
        entity.setId(1L);
        entity.setDictType("sys_user_status");
        when(sysDictTypeMapper.selectById(1L)).thenReturn(entity);
        when(sysDictDataMapper.countByDictType("sys_user_status")).thenReturn(0L);

        dictTypeService.deleteDictType(1L);

        verify(systemCacheEvictor).evictSystemDictTypes();
        verify(systemCacheEvictor).evictSystemDictData("sys_user_status");
    }

    @Test
    void shouldRejectInvalidDictTypePattern() {
        DictTypeSaveRequest request = buildRequest();
        request.setDictType("SysUserStatus");

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> dictTypeService.createDictType(request));

        Assertions.assertEquals("字典类型仅支持小写字母、数字和下划线，且需以字母开头", exception.getMessage());
    }

    private DictTypeSaveRequest buildRequest() {
        DictTypeSaveRequest request = new DictTypeSaveRequest();
        request.setDictName("用户状态");
        request.setDictType("sys_user_status");
        request.setStatus(1);
        request.setRemark("测试");
        return request;
    }
}

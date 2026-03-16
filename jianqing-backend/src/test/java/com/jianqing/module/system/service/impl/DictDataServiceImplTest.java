package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.dto.DictDataSaveRequest;
import com.jianqing.module.system.dto.DictDataSummary;
import com.jianqing.module.system.dto.DictOptionItem;
import com.jianqing.module.system.entity.SysDictData;
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
class DictDataServiceImplTest {

    @Mock
    private SysDictDataMapper sysDictDataMapper;

    @Mock
    private SysDictTypeMapper sysDictTypeMapper;

    @Mock
    private SystemCacheEvictor systemCacheEvictor;

    private DictDataServiceImpl dictDataService;

    @BeforeEach
    void setUp() {
        dictDataService = new DictDataServiceImpl(sysDictDataMapper, sysDictTypeMapper, systemCacheEvictor);
    }

    @Test
    void shouldListDictData() {
        SysDictData entity = new SysDictData();
        entity.setId(1L);
        entity.setDictType("sys_user_status");
        entity.setLabel("启用");
        entity.setValue("1");
        entity.setSortNo(1);
        entity.setStatus(1);
        when(sysDictDataMapper.selectByDictType("sys_user_status")).thenReturn(List.of(entity));

        List<DictDataSummary> result = dictDataService.listDictData("sys_user_status");

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("启用", result.get(0).label());
    }

    @Test
    void shouldCreateDictData() {
        when(sysDictTypeMapper.countByDictType("sys_user_status", null)).thenReturn(1L);
        when(sysDictDataMapper.countByDictTypeAndValue("sys_user_status", "1", null)).thenReturn(0L);
        when(sysDictDataMapper.insert(any(SysDictData.class))).thenAnswer(invocation -> {
            SysDictData entity = invocation.getArgument(0);
            entity.setId(12L);
            return 1;
        });

        DictDataSummary result = dictDataService.createDictData(buildRequest());

        Assertions.assertEquals(12L, result.id());
        Assertions.assertEquals("1", result.value());
        ArgumentCaptor<SysDictData> captor = ArgumentCaptor.forClass(SysDictData.class);
        verify(sysDictDataMapper).insert(captor.capture());
        Assertions.assertEquals("启用", captor.getValue().getLabel());
        verify(systemCacheEvictor).evictSystemDictData("sys_user_status");
    }

    @Test
    void shouldRejectCreateWhenDictTypeMissing() {
        when(sysDictTypeMapper.countByDictType("sys_user_status", null)).thenReturn(0L);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> dictDataService.createDictData(buildRequest()));

        Assertions.assertEquals("字典类型不存在", exception.getMessage());
    }

    @Test
    void shouldReturnEnabledOptions() {
        SysDictData entity = new SysDictData();
        entity.setLabel("启用");
        entity.setValue("1");
        entity.setColorType("success");
        entity.setCssClass("is-enabled");
        when(sysDictDataMapper.selectEnabledByDictType("sys_user_status")).thenReturn(List.of(entity));

        List<DictOptionItem> result = dictDataService.listEnabledOptions("sys_user_status");

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("success", result.get(0).colorType());
    }

    @Test
    void shouldEvictOldAndNewCacheWhenUpdateDictTypeChanged() {
        SysDictData entity = new SysDictData();
        entity.setId(1L);
        entity.setDictType("sys_user_status");
        when(sysDictDataMapper.selectById(1L)).thenReturn(entity);
        when(sysDictTypeMapper.countByDictType("sys_account_status", null)).thenReturn(1L);
        when(sysDictDataMapper.countByDictTypeAndValue("sys_account_status", "1", 1L)).thenReturn(0L);

        DictDataSaveRequest request = buildRequest();
        request.setDictType("sys_account_status");
        dictDataService.updateDictData(1L, request);

        verify(systemCacheEvictor).evictSystemDictData("sys_user_status");
        verify(systemCacheEvictor).evictSystemDictData("sys_account_status");
    }

    @Test
    void shouldEvictCacheWhenDeleteDictData() {
        SysDictData entity = new SysDictData();
        entity.setId(1L);
        entity.setDictType("sys_user_status");
        when(sysDictDataMapper.selectById(1L)).thenReturn(entity);

        dictDataService.deleteDictData(1L);

        verify(systemCacheEvictor).evictSystemDictData("sys_user_status");
    }

    private DictDataSaveRequest buildRequest() {
        DictDataSaveRequest request = new DictDataSaveRequest();
        request.setDictType("sys_user_status");
        request.setLabel("启用");
        request.setValue("1");
        request.setColorType("success");
        request.setCssClass("is-enabled");
        request.setSortNo(1);
        request.setStatus(1);
        request.setRemark("测试");
        return request;
    }
}

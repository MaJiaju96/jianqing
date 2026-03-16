package com.jianqing.module.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jianqing.module.system.dto.DictDataSaveRequest;
import com.jianqing.module.system.dto.DictDataSummary;
import com.jianqing.module.system.dto.DictOptionItem;
import com.jianqing.module.system.entity.SysDictData;
import com.jianqing.module.system.mapper.SysDictDataMapper;
import com.jianqing.module.system.mapper.SysDictTypeMapper;
import com.jianqing.module.system.service.DictDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements DictDataService {

    private final SysDictTypeMapper sysDictTypeMapper;
    private final SystemCacheEvictor systemCacheEvictor;

    public DictDataServiceImpl(SysDictDataMapper sysDictDataMapper,
                               SysDictTypeMapper sysDictTypeMapper,
                               SystemCacheEvictor systemCacheEvictor) {
        this.baseMapper = sysDictDataMapper;
        this.sysDictTypeMapper = sysDictTypeMapper;
        this.systemCacheEvictor = systemCacheEvictor;
    }

    @Override
    public List<DictDataSummary> listDictData(String dictType) {
        return baseMapper.selectByDictType(normalizeRequiredText(dictType)).stream().map(this::toSummary).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictDataSummary createDictData(DictDataSaveRequest request) {
        String dictType = normalizeRequiredText(request.getDictType());
        validateDictTypeExists(dictType);
        validateValueUnique(dictType, normalizeRequiredText(request.getValue()), null);
        SysDictData entity = new SysDictData();
        fillEntity(entity, request, dictType);
        baseMapper.insert(entity);
        systemCacheEvictor.evictSystemDictData(dictType);
        return toSummary(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictDataSummary updateDictData(Long id, DictDataSaveRequest request) {
        SysDictData entity = getDictDataOrThrow(id);
        String oldDictType = entity.getDictType();
        String dictType = normalizeRequiredText(request.getDictType());
        String value = normalizeRequiredText(request.getValue());
        validateDictTypeExists(dictType);
        validateValueUnique(dictType, value, id);
        fillEntity(entity, request, dictType);
        entity.setValue(value);
        baseMapper.updateById(entity);
        systemCacheEvictor.evictSystemDictData(oldDictType);
        if (!oldDictType.equals(dictType)) {
            systemCacheEvictor.evictSystemDictData(dictType);
        }
        return toSummary(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictData(Long id) {
        SysDictData entity = getDictDataOrThrow(id);
        baseMapper.deleteById(id);
        systemCacheEvictor.evictSystemDictData(entity.getDictType());
    }

    @Override
    public List<DictOptionItem> listEnabledOptions(String dictType) {
        return baseMapper.selectEnabledByDictType(normalizeRequiredText(dictType)).stream()
                .map(item -> new DictOptionItem(item.getLabel(), item.getValue(), item.getColorType(), item.getCssClass()))
                .toList();
    }

    private void validateDictTypeExists(String dictType) {
        Long count = sysDictTypeMapper.countByDictType(dictType, null);
        if (count == null || count <= 0) {
            throw new IllegalArgumentException("字典类型不存在");
        }
    }

    private void validateValueUnique(String dictType, String value, Long excludeId) {
        Long count = baseMapper.countByDictTypeAndValue(dictType, value, excludeId);
        if (count != null && count > 0) {
            throw new IllegalArgumentException("同一字典类型下的键值已存在");
        }
    }

    private SysDictData getDictDataOrThrow(Long id) {
        SysDictData entity = baseMapper.selectById(id);
        if (entity == null) {
            throw new IllegalArgumentException("字典数据不存在");
        }
        return entity;
    }

    private void fillEntity(SysDictData entity, DictDataSaveRequest request, String dictType) {
        entity.setDictType(dictType);
        entity.setLabel(normalizeRequiredText(request.getLabel()));
        entity.setValue(normalizeRequiredText(request.getValue()));
        entity.setColorType(safeText(request.getColorType()));
        entity.setCssClass(safeText(request.getCssClass()));
        entity.setSortNo(request.getSortNo());
        entity.setStatus(request.getStatus());
        entity.setRemark(safeText(request.getRemark()));
    }

    private String normalizeRequiredText(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private DictDataSummary toSummary(SysDictData entity) {
        return new DictDataSummary(entity.getId(), entity.getDictType(), entity.getLabel(), entity.getValue(),
                entity.getColorType(), entity.getCssClass(), entity.getSortNo(), entity.getStatus(), entity.getRemark());
    }
}

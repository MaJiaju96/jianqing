package com.jianqing.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jianqing.module.system.dto.DictTypeSaveRequest;
import com.jianqing.module.system.dto.DictTypeSummary;
import com.jianqing.module.system.entity.SysDictType;
import com.jianqing.module.system.mapper.SysDictDataMapper;
import com.jianqing.module.system.mapper.SysDictTypeMapper;
import com.jianqing.module.system.service.DictTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class DictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements DictTypeService {

    private static final Pattern DICT_TYPE_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*$");

    private final SysDictDataMapper sysDictDataMapper;
    private final SystemCacheEvictor systemCacheEvictor;

    public DictTypeServiceImpl(SysDictTypeMapper sysDictTypeMapper,
                               SysDictDataMapper sysDictDataMapper,
                               SystemCacheEvictor systemCacheEvictor) {
        this.baseMapper = sysDictTypeMapper;
        this.sysDictDataMapper = sysDictDataMapper;
        this.systemCacheEvictor = systemCacheEvictor;
    }

    @Override
    public List<DictTypeSummary> listDictTypes() {
        return baseMapper.selectAllDictTypes().stream().map(this::toSummary).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictTypeSummary createDictType(DictTypeSaveRequest request) {
        String dictType = normalizeDictType(request.getDictType());
        validateDictType(dictType, null);
        SysDictType entity = new SysDictType();
        fillEntity(entity, request, dictType);
        baseMapper.insert(entity);
        systemCacheEvictor.evictSystemDictTypes();
        return toSummary(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictTypeSummary updateDictType(Long id, DictTypeSaveRequest request) {
        SysDictType entity = getDictTypeOrThrow(id);
        String oldDictType = entity.getDictType();
        String newDictType = normalizeDictType(request.getDictType());
        validateDictType(newDictType, id);
        fillEntity(entity, request, newDictType);
        baseMapper.updateById(entity);
        systemCacheEvictor.evictSystemDictTypes();
        systemCacheEvictor.evictSystemDictData(oldDictType);
        if (!oldDictType.equals(newDictType)) {
            sysDictDataMapper.updateDictType(oldDictType, newDictType);
            systemCacheEvictor.evictSystemDictData(newDictType);
        }
        return toSummary(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictType(Long id) {
        SysDictType entity = getDictTypeOrThrow(id);
        Long dataCount = sysDictDataMapper.countByDictType(entity.getDictType());
        if (dataCount != null && dataCount > 0) {
            throw new IllegalArgumentException("当前字典类型下仍存在字典数据，无法删除");
        }
        baseMapper.deleteById(id);
        systemCacheEvictor.evictSystemDictTypes();
        systemCacheEvictor.evictSystemDictData(entity.getDictType());
    }

    @Override
    public boolean existsByDictType(String dictType) {
        if (dictType == null || dictType.isBlank()) {
            return false;
        }
        return getOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType.trim())
                .last("limit 1")) != null;
    }

    private void validateDictType(String dictType, Long excludeId) {
        if (!DICT_TYPE_PATTERN.matcher(dictType).matches()) {
            throw new IllegalArgumentException("字典类型仅支持小写字母、数字和下划线，且需以字母开头");
        }
        Long count = baseMapper.countByDictType(dictType, excludeId);
        if (count != null && count > 0) {
            throw new IllegalArgumentException("字典类型已存在");
        }
    }

    private SysDictType getDictTypeOrThrow(Long id) {
        SysDictType entity = baseMapper.selectById(id);
        if (entity == null) {
            throw new IllegalArgumentException("字典类型不存在");
        }
        return entity;
    }

    private void fillEntity(SysDictType entity, DictTypeSaveRequest request, String dictType) {
        entity.setDictName(safeRequiredText(request.getDictName()));
        entity.setDictType(dictType);
        entity.setStatus(request.getStatus());
        entity.setRemark(safeText(request.getRemark()));
    }

    private String normalizeDictType(String dictType) {
        return safeRequiredText(dictType);
    }

    private String safeRequiredText(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private DictTypeSummary toSummary(SysDictType entity) {
        return new DictTypeSummary(entity.getId(), entity.getDictName(), entity.getDictType(),
                entity.getStatus(), entity.getRemark());
    }
}

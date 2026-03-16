package com.jianqing.module.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jianqing.integration.config.DynamicConfigGateway;
import com.jianqing.module.system.dto.ConfigHistorySummary;
import com.jianqing.module.system.dto.ConfigDiffItem;
import com.jianqing.module.system.dto.ConfigDiffSummary;
import com.jianqing.module.system.dto.ConfigRestorePreviewSummary;
import com.jianqing.module.system.dto.ConfigSaveRequest;
import com.jianqing.module.system.dto.ConfigSummary;
import com.jianqing.module.system.entity.SysConfig;
import com.jianqing.module.system.entity.SysConfigHistory;
import com.jianqing.module.system.mapper.SysConfigHistoryMapper;
import com.jianqing.module.system.mapper.SysConfigMapper;
import com.jianqing.module.system.service.ConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ConfigService {

    private static final Pattern CONFIG_KEY_PATTERN = Pattern.compile("^[a-z][a-z0-9_.-]*$");
    private static final Pattern CONFIG_GROUP_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{1,63}$");
    private static final Set<String> ALLOWED_VALUE_TYPES = Set.of("string", "number", "boolean", "json");

    private final DynamicConfigGateway dynamicConfigGateway;
    private final SystemCacheEvictor systemCacheEvictor;
    private final SysConfigHistoryMapper sysConfigHistoryMapper;

    public ConfigServiceImpl(SysConfigMapper sysConfigMapper,
                             DynamicConfigGateway dynamicConfigGateway,
                             SystemCacheEvictor systemCacheEvictor,
                             SysConfigHistoryMapper sysConfigHistoryMapper) {
        this.baseMapper = sysConfigMapper;
        this.dynamicConfigGateway = dynamicConfigGateway;
        this.systemCacheEvictor = systemCacheEvictor;
        this.sysConfigHistoryMapper = sysConfigHistoryMapper;
    }

    @Override
    public List<ConfigSummary> listConfigs() {
        return baseMapper.selectAllConfigs().stream().map(this::toSummary).toList();
    }

    @Override
    public List<ConfigHistorySummary> listConfigHistory(Long configId) {
        return sysConfigHistoryMapper.selectByConfigId(configId).stream().map(this::toHistorySummary).toList();
    }

    @Override
    public List<ConfigHistorySummary> listDeletedConfigHistory() {
        return sysConfigHistoryMapper.selectLatestDeletedHistories().stream().map(this::toHistorySummary).toList();
    }

    @Override
    public ConfigRestorePreviewSummary previewDeletedConfigRestore(Long historyId) {
        SysConfigHistory history = sysConfigHistoryMapper.selectDeleteHistoryById(historyId);
        if (history == null) {
            throw new IllegalArgumentException("删除历史不存在");
        }
        SysConfig existing = baseMapper.selectByConfigKey(safeRequiredText(history.getConfigKey()));
        List<ConfigDiffItem> items = existing == null ? List.of() : List.of(
                buildDiffItem("configName", "参数名称", existing.getConfigName(), history.getConfigName()),
                buildDiffItem("configGroup", "参数分组", existing.getConfigGroup(), history.getConfigGroup()),
                buildDiffItem("configValue", "参数值", existing.getConfigValue(), history.getConfigValue()),
                buildDiffItem("valueType", "值类型", existing.getValueType(), history.getValueType()),
                buildDiffItem("isBuiltin", "内置参数", builtinText(existing.getIsBuiltin()), builtinText(history.getIsBuiltin()))
        );
        return new ConfigRestorePreviewSummary(historyId, history.getConfigKey(), history.getConfigName(),
                history.getConfigGroup(), history.getConfigValue(), history.getValueType(), history.getIsBuiltin(),
                history.getChangeNote(),
                history.getCreatedAt() == null ? "" : history.getCreatedAt().toString(),
                existing != null, existing == null ? null : existing.getId(),
                existing == null ? "" : existing.getConfigName(), items);
    }

    @Override
    public ConfigDiffSummary diffConfig(Long configId, Long historyId, Long compareHistoryId) {
        SysConfigHistory history = sysConfigHistoryMapper.selectByIdAndConfigId(historyId, configId);
        if (history == null) {
            throw new IllegalArgumentException("对比历史不存在");
        }
        if (compareHistoryId == null) {
            SysConfig entity = getConfigOrThrow(configId);
            List<ConfigDiffItem> items = List.of(
                    buildDiffItem("configName", "参数名称", entity.getConfigName(), history.getConfigName()),
                    buildDiffItem("configGroup", "参数分组", entity.getConfigGroup(), history.getConfigGroup()),
                    buildDiffItem("configValue", "参数值", entity.getConfigValue(), history.getConfigValue()),
                    buildDiffItem("valueType", "值类型", entity.getValueType(), history.getValueType()),
                    buildDiffItem("isBuiltin", "内置参数", builtinText(entity.getIsBuiltin()), builtinText(history.getIsBuiltin()))
            );
            return new ConfigDiffSummary(configId, historyId, null, history.getConfigKey(), history.getChangeType(),
                    history.getCreatedAt() == null ? "" : history.getCreatedAt().toString(),
                    null, null, true, items);
        }
        SysConfigHistory compareHistory = sysConfigHistoryMapper.selectByIdAndConfigId(compareHistoryId, configId);
        if (compareHistory == null) {
            throw new IllegalArgumentException("基准历史不存在");
        }
        List<ConfigDiffItem> items = List.of(
                buildDiffItem("configName", "参数名称", history.getConfigName(), compareHistory.getConfigName()),
                buildDiffItem("configGroup", "参数分组", history.getConfigGroup(), compareHistory.getConfigGroup()),
                buildDiffItem("configValue", "参数值", history.getConfigValue(), compareHistory.getConfigValue()),
                buildDiffItem("valueType", "值类型", history.getValueType(), compareHistory.getValueType()),
                buildDiffItem("isBuiltin", "内置参数", builtinText(history.getIsBuiltin()), builtinText(compareHistory.getIsBuiltin()))
        );
        return new ConfigDiffSummary(configId, historyId, compareHistoryId, history.getConfigKey(), history.getChangeType(),
                history.getCreatedAt() == null ? "" : history.getCreatedAt().toString(),
                compareHistory.getChangeType(), compareHistory.getCreatedAt() == null ? "" : compareHistory.getCreatedAt().toString(),
                false, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigSummary rollbackConfig(Long configId, Long historyId) {
        SysConfig entity = getConfigOrThrow(configId);
        SysConfigHistory history = sysConfigHistoryMapper.selectByIdAndConfigId(historyId, configId);
        if (history == null) {
            throw new IllegalArgumentException("回滚历史不存在");
        }
        if ("DELETE".equals(history.getChangeType())) {
            throw new IllegalArgumentException("删除记录不支持直接回滚");
        }
        entity.setConfigValue(safeRequiredText(history.getConfigValue()));
        entity.setConfigName(safeRequiredText(history.getConfigName()));
        entity.setConfigGroup(normalizeConfigGroup(history.getConfigGroup()));
        entity.setValueType(safeRequiredText(history.getValueType()));
        entity.setIsBuiltin(history.getIsBuiltin());
        baseMapper.updateById(entity);
        recordHistory(entity, "ROLLBACK", "回滚到历史版本#" + historyId);
        publishConfig(entity);
        systemCacheEvictor.evictSystemConfigs();
        return toSummary(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigSummary restoreDeletedConfig(Long historyId) {
        SysConfigHistory history = sysConfigHistoryMapper.selectDeleteHistoryById(historyId);
        if (history == null) {
            throw new IllegalArgumentException("删除历史不存在");
        }
        validateConfigKey(safeRequiredText(history.getConfigKey()), null);
        validateConfigGroup(history.getConfigGroup());
        validateValueType(history.getValueType());
        SysConfig entity = new SysConfig();
        entity.setConfigKey(safeRequiredText(history.getConfigKey()));
        entity.setConfigValue(safeRequiredText(history.getConfigValue()));
        entity.setConfigName(safeRequiredText(history.getConfigName()));
        entity.setConfigGroup(normalizeConfigGroup(history.getConfigGroup()));
        entity.setValueType(safeRequiredText(history.getValueType()));
        entity.setIsBuiltin(history.getIsBuiltin());
        entity.setRemark("已从删除历史#" + historyId + "恢复");
        baseMapper.insert(entity);
        recordHistory(entity, "RESTORE", "从删除历史#" + historyId + "恢复参数");
        publishConfig(entity);
        systemCacheEvictor.evictSystemConfigs();
        return toSummary(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigSummary createConfig(ConfigSaveRequest request) {
        String configKey = normalizeConfigKey(request.getConfigKey());
        validateConfigKey(configKey, null);
        validateConfigGroup(request.getConfigGroup());
        validateValueType(request.getValueType());
        SysConfig entity = new SysConfig();
        fillEntity(entity, request, configKey);
        baseMapper.insert(entity);
        recordHistory(entity, "CREATE", "新增参数");
        publishConfig(entity);
        systemCacheEvictor.evictSystemConfigs();
        return toSummary(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigSummary updateConfig(Long id, ConfigSaveRequest request) {
        SysConfig entity = getConfigOrThrow(id);
        String oldConfigKey = entity.getConfigKey();
        String configKey = normalizeConfigKey(request.getConfigKey());
        validateConfigKey(configKey, id);
        validateConfigGroup(request.getConfigGroup());
        validateValueType(request.getValueType());
        if (entity.getIsBuiltin() != null && entity.getIsBuiltin() == 1 && !oldConfigKey.equals(configKey)) {
            throw new IllegalArgumentException("内置参数不允许修改参数键");
        }
        fillEntity(entity, request, configKey);
        baseMapper.updateById(entity);
        recordHistory(entity, "UPDATE", "更新参数");
        publishConfig(entity);
        systemCacheEvictor.evictSystemConfigs();
        return toSummary(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        SysConfig entity = getConfigOrThrow(id);
        if (entity.getIsBuiltin() != null && entity.getIsBuiltin() == 1) {
            throw new IllegalArgumentException("内置参数不允许删除");
        }
        recordHistory(entity, "DELETE", "删除参数");
        baseMapper.deleteById(id);
        dynamicConfigGateway.publish(entity.getConfigKey(), normalizeConfigGroup(entity.getConfigGroup()), "");
        systemCacheEvictor.evictSystemConfigs();
    }

    private void validateConfigKey(String configKey, Long excludeId) {
        if (!CONFIG_KEY_PATTERN.matcher(configKey).matches()) {
            throw new IllegalArgumentException("参数键仅支持小写字母开头，后续可包含小写字母、数字、点、横线与下划线");
        }
        Long count = baseMapper.countByConfigKey(configKey, excludeId);
        if (count != null && count > 0) {
            throw new IllegalArgumentException("参数键已存在");
        }
    }

    private void validateValueType(String valueType) {
        String normalized = safeRequiredText(valueType);
        if (!ALLOWED_VALUE_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("值类型仅支持 string/number/boolean/json");
        }
    }

    private void validateConfigGroup(String configGroup) {
        String normalized = normalizeConfigGroup(configGroup);
        if (!CONFIG_GROUP_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("参数分组仅支持大写字母开头，后续可包含大写字母、数字与下划线");
        }
    }

    private SysConfig getConfigOrThrow(Long id) {
        SysConfig entity = baseMapper.selectById(id);
        if (entity == null) {
            throw new IllegalArgumentException("参数不存在");
        }
        return entity;
    }

    private void fillEntity(SysConfig entity, ConfigSaveRequest request, String configKey) {
        entity.setConfigKey(configKey);
        entity.setConfigValue(safeRequiredText(request.getConfigValue()));
        entity.setConfigName(safeRequiredText(request.getConfigName()));
        entity.setConfigGroup(normalizeConfigGroup(request.getConfigGroup()));
        entity.setValueType(safeRequiredText(request.getValueType()));
        entity.setIsBuiltin(request.getIsBuiltin());
        entity.setRemark(safeText(request.getRemark()));
    }

    private void publishConfig(SysConfig entity) {
        dynamicConfigGateway.publish(entity.getConfigKey(), normalizeConfigGroup(entity.getConfigGroup()), entity.getConfigValue());
    }

    private String normalizeConfigKey(String configKey) {
        return safeRequiredText(configKey);
    }

    private String normalizeConfigGroup(String configGroup) {
        return safeRequiredText(configGroup).toUpperCase();
    }

    private String safeRequiredText(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private void recordHistory(SysConfig entity, String changeType, String changeNote) {
        SysConfigHistory history = new SysConfigHistory();
        history.setConfigId(entity.getId());
        history.setConfigKey(entity.getConfigKey());
        history.setConfigName(entity.getConfigName());
        history.setConfigGroup(entity.getConfigGroup());
        history.setConfigValue(entity.getConfigValue());
        history.setValueType(entity.getValueType());
        history.setIsBuiltin(entity.getIsBuiltin());
        history.setChangeType(changeType);
        history.setChangeNote(changeNote);
        sysConfigHistoryMapper.insert(history);
    }

    private ConfigDiffItem buildDiffItem(String field, String fieldLabel, String currentValue, String historyValue) {
        return new ConfigDiffItem(field, fieldLabel, safeText(currentValue), safeText(historyValue),
                !Objects.equals(safeText(currentValue), safeText(historyValue)));
    }

    private String builtinText(Integer isBuiltin) {
        return isBuiltin != null && isBuiltin == 1 ? "是" : "否";
    }

    private ConfigSummary toSummary(SysConfig entity) {
        return new ConfigSummary(entity.getId(), entity.getConfigKey(), entity.getConfigValue(), entity.getConfigName(),
                entity.getConfigGroup(), entity.getValueType(), entity.getIsBuiltin(), entity.getRemark());
    }

    private ConfigHistorySummary toHistorySummary(SysConfigHistory entity) {
        return new ConfigHistorySummary(entity.getId(), entity.getConfigId(), entity.getConfigKey(), entity.getConfigName(),
                entity.getConfigGroup(), entity.getConfigValue(), entity.getValueType(), entity.getIsBuiltin(),
                entity.getChangeType(), entity.getChangeNote(), entity.getCreatedAt() == null ? "" : entity.getCreatedAt().toString());
    }
}

package com.jianqing.module.system.service.impl;

import com.jianqing.integration.config.DynamicConfigGateway;
import com.jianqing.module.system.dto.ConfigHistorySummary;
import com.jianqing.module.system.dto.ConfigRestorePreviewSummary;
import com.jianqing.module.system.dto.ConfigSaveRequest;
import com.jianqing.module.system.dto.ConfigDiffSummary;
import com.jianqing.module.system.entity.SysConfigHistory;
import com.jianqing.module.system.dto.ConfigSummary;
import com.jianqing.module.system.entity.SysConfig;
import com.jianqing.module.system.mapper.SysConfigHistoryMapper;
import com.jianqing.module.system.mapper.SysConfigMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
class ConfigServiceImplTest {

    @Mock
    private SysConfigMapper sysConfigMapper;

    @Mock
    private DynamicConfigGateway dynamicConfigGateway;

    @Mock
    private SystemCacheEvictor systemCacheEvictor;

    @Mock
    private SysConfigHistoryMapper sysConfigHistoryMapper;

    private ConfigServiceImpl configService;

    @BeforeEach
    void setUp() {
        configService = new ConfigServiceImpl(sysConfigMapper, dynamicConfigGateway, systemCacheEvictor, sysConfigHistoryMapper);
    }

    @Test
    void shouldListConfigs() {
        SysConfig entity = new SysConfig();
        entity.setId(1L);
        entity.setConfigKey("sys.theme.default");
        entity.setConfigValue("midnight-blue");
        entity.setConfigName("默认主题");
        entity.setConfigGroup("UI");
        entity.setValueType("string");
        entity.setIsBuiltin(1);
        when(sysConfigMapper.selectAllConfigs()).thenReturn(List.of(entity));

        List<ConfigSummary> result = configService.listConfigs();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("sys.theme.default", result.get(0).configKey());
    }

    @Test
    void shouldCreateConfigAndPublish() {
        when(sysConfigMapper.countByConfigKey("sys.theme.default", null)).thenReturn(0L);
        when(sysConfigMapper.insert(any(SysConfig.class))).thenAnswer(invocation -> {
            SysConfig entity = invocation.getArgument(0);
            entity.setId(2L);
            return 1;
        });

        ConfigSummary result = configService.createConfig(buildRequest());

        Assertions.assertEquals(2L, result.id());
        verify(dynamicConfigGateway).publish("sys.theme.default", "UI", "midnight-blue");
        verify(systemCacheEvictor).evictSystemConfigs();
        verify(sysConfigHistoryMapper).insert(any(SysConfigHistory.class));
        ArgumentCaptor<SysConfig> captor = ArgumentCaptor.forClass(SysConfig.class);
        verify(sysConfigMapper).insert(captor.capture());
        Assertions.assertEquals("默认主题", captor.getValue().getConfigName());
    }

    @Test
    void shouldRejectBuiltinKeyChange() {
        SysConfig entity = new SysConfig();
        entity.setId(1L);
        entity.setConfigKey("sys.theme.default");
        entity.setConfigGroup("UI");
        entity.setIsBuiltin(1);
        when(sysConfigMapper.selectById(1L)).thenReturn(entity);
        when(sysConfigMapper.countByConfigKey("sys.theme.next", 1L)).thenReturn(0L);

        ConfigSaveRequest request = buildRequest();
        request.setConfigKey("sys.theme.next");

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> configService.updateConfig(1L, request));

        Assertions.assertEquals("内置参数不允许修改参数键", exception.getMessage());
    }

    @Test
    void shouldRejectDeleteBuiltinConfig() {
        SysConfig entity = new SysConfig();
        entity.setId(1L);
        entity.setConfigKey("sys.theme.default");
        entity.setConfigGroup("UI");
        entity.setIsBuiltin(1);
        when(sysConfigMapper.selectById(1L)).thenReturn(entity);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> configService.deleteConfig(1L));

        Assertions.assertEquals("内置参数不允许删除", exception.getMessage());
    }

    @Test
    void shouldRollbackConfigFromHistory() {
        SysConfig entity = new SysConfig();
        entity.setId(1L);
        entity.setConfigKey("sys.theme.default");
        entity.setConfigName("默认主题");
        entity.setConfigGroup("UI");
        entity.setConfigValue("light");
        entity.setValueType("string");
        entity.setIsBuiltin(1);
        when(sysConfigMapper.selectById(1L)).thenReturn(entity);

        SysConfigHistory history = new SysConfigHistory();
        history.setId(9L);
        history.setConfigId(1L);
        history.setConfigKey("sys.theme.default");
        history.setConfigName("默认主题");
        history.setConfigGroup("UI");
        history.setConfigValue("midnight-blue");
        history.setValueType("string");
        history.setIsBuiltin(1);
        history.setChangeType("UPDATE");
        when(sysConfigHistoryMapper.selectByIdAndConfigId(9L, 1L)).thenReturn(history);

        ConfigSummary result = configService.rollbackConfig(1L, 9L);

        Assertions.assertEquals("midnight-blue", result.configValue());
        verify(dynamicConfigGateway).publish("sys.theme.default", "UI", "midnight-blue");
        verify(systemCacheEvictor).evictSystemConfigs();
        verify(sysConfigHistoryMapper).insert(any(SysConfigHistory.class));
    }

    @Test
    void shouldListDeletedConfigHistory() {
        SysConfigHistory history = new SysConfigHistory();
        history.setId(11L);
        history.setConfigId(3L);
        history.setConfigKey("sys.login.captcha.enabled");
        history.setConfigName("登录验证码开关");
        history.setConfigGroup("AUTH");
        history.setConfigValue("false");
        history.setValueType("boolean");
        history.setIsBuiltin(0);
        history.setChangeType("DELETE");
        when(sysConfigHistoryMapper.selectLatestDeletedHistories()).thenReturn(List.of(history));

        Assertions.assertEquals(1, configService.listDeletedConfigHistory().size());
        Assertions.assertEquals("DELETE", configService.listDeletedConfigHistory().get(0).changeType());
    }

    @Test
    void shouldPreviewDeletedConfigRestore() {
        SysConfigHistory history = new SysConfigHistory();
        history.setId(11L);
        history.setConfigId(3L);
        history.setConfigKey("sys.login.captcha.enabled");
        history.setConfigName("登录验证码开关");
        history.setConfigGroup("AUTH");
        history.setConfigValue("false");
        history.setValueType("boolean");
        history.setIsBuiltin(0);
        history.setChangeType("DELETE");
        when(sysConfigHistoryMapper.selectDeleteHistoryById(11L)).thenReturn(history);

        ConfigRestorePreviewSummary summary = configService.previewDeletedConfigRestore(11L);

        Assertions.assertEquals(11L, summary.historyId());
        Assertions.assertEquals("sys.login.captcha.enabled", summary.configKey());
        Assertions.assertEquals("登录验证码开关", summary.configName());
        Assertions.assertFalse(summary.keyOccupied());
        Assertions.assertEquals(0, summary.items().size());
    }

    @Test
    void shouldPreviewDeletedConfigRestoreWithExistingConfigDiff() {
        SysConfigHistory history = new SysConfigHistory();
        history.setId(11L);
        history.setConfigKey("sys.login.captcha.enabled");
        history.setConfigName("登录验证码开关");
        history.setConfigGroup("AUTH");
        history.setConfigValue("false");
        history.setValueType("boolean");
        history.setIsBuiltin(0);
        when(sysConfigHistoryMapper.selectDeleteHistoryById(11L)).thenReturn(history);

        SysConfig existing = new SysConfig();
        existing.setId(9L);
        existing.setConfigKey("sys.login.captcha.enabled");
        existing.setConfigName("登录验证码开关-当前");
        existing.setConfigGroup("AUTH");
        existing.setConfigValue("true");
        existing.setValueType("boolean");
        existing.setIsBuiltin(1);
        when(sysConfigMapper.selectByConfigKey("sys.login.captcha.enabled")).thenReturn(existing);

        ConfigRestorePreviewSummary summary = configService.previewDeletedConfigRestore(11L);

        Assertions.assertTrue(summary.keyOccupied());
        Assertions.assertEquals(9L, summary.existingConfigId());
        Assertions.assertTrue(summary.items().stream().anyMatch(item -> "configValue".equals(item.field()) && item.changed()));
    }

    @Test
    void shouldRestoreDeletedConfigFromHistory() {
        when(sysConfigMapper.countByConfigKey("sys.login.captcha.enabled", null)).thenReturn(0L);
        when(sysConfigMapper.insert(any(SysConfig.class))).thenAnswer(invocation -> {
            SysConfig entity = invocation.getArgument(0);
            entity.setId(8L);
            return 1;
        });

        SysConfigHistory history = new SysConfigHistory();
        history.setId(11L);
        history.setConfigId(3L);
        history.setConfigKey("sys.login.captcha.enabled");
        history.setConfigName("登录验证码开关");
        history.setConfigGroup("AUTH");
        history.setConfigValue("false");
        history.setValueType("boolean");
        history.setIsBuiltin(0);
        history.setChangeType("DELETE");
        when(sysConfigHistoryMapper.selectDeleteHistoryById(11L)).thenReturn(history);

        ConfigSummary result = configService.restoreDeletedConfig(11L);

        Assertions.assertEquals(8L, result.id());
        Assertions.assertEquals("sys.login.captcha.enabled", result.configKey());
        verify(dynamicConfigGateway).publish("sys.login.captcha.enabled", "AUTH", "false");
        verify(systemCacheEvictor).evictSystemConfigs();
        verify(sysConfigHistoryMapper).insert(any(SysConfigHistory.class));
    }

    @Test
    void shouldRejectRestoreWhenDeleteHistoryMissing() {
        when(sysConfigHistoryMapper.selectDeleteHistoryById(11L)).thenReturn(null);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> configService.restoreDeletedConfig(11L));

        Assertions.assertEquals("删除历史不存在", exception.getMessage());
    }

    @Test
    void shouldBuildDiffAgainstHistory() {
        SysConfig entity = new SysConfig();
        entity.setId(1L);
        entity.setConfigKey("sys.theme.default");
        entity.setConfigName("默认主题");
        entity.setConfigGroup("UI");
        entity.setConfigValue("light");
        entity.setValueType("string");
        entity.setIsBuiltin(1);
        when(sysConfigMapper.selectById(1L)).thenReturn(entity);

        SysConfigHistory history = new SysConfigHistory();
        history.setId(9L);
        history.setConfigId(1L);
        history.setConfigName("默认主题");
        history.setConfigGroup("UI");
        history.setConfigValue("midnight-blue");
        history.setValueType("string");
        history.setIsBuiltin(1);
        history.setChangeType("UPDATE");
        when(sysConfigHistoryMapper.selectByIdAndConfigId(9L, 1L)).thenReturn(history);

        ConfigDiffSummary result = configService.diffConfig(1L, 9L, null);

        Assertions.assertEquals(5, result.items().size());
        Assertions.assertTrue(result.compareWithCurrent());
        Assertions.assertTrue(result.items().stream().anyMatch(item -> "configValue".equals(item.field()) && item.changed()));
    }

    @Test
    void shouldBuildDiffBetweenHistories() {
        SysConfigHistory leftHistory = new SysConfigHistory();
        leftHistory.setId(9L);
        leftHistory.setConfigId(1L);
        leftHistory.setConfigKey("sys.theme.default");
        leftHistory.setConfigName("默认主题");
        leftHistory.setConfigGroup("UI");
        leftHistory.setConfigValue("light");
        leftHistory.setValueType("string");
        leftHistory.setIsBuiltin(1);
        leftHistory.setChangeType("UPDATE");
        when(sysConfigHistoryMapper.selectByIdAndConfigId(9L, 1L)).thenReturn(leftHistory);

        SysConfigHistory rightHistory = new SysConfigHistory();
        rightHistory.setId(7L);
        rightHistory.setConfigId(1L);
        rightHistory.setConfigKey("sys.theme.default");
        rightHistory.setConfigName("默认主题");
        rightHistory.setConfigGroup("UI");
        rightHistory.setConfigValue("midnight-blue");
        rightHistory.setValueType("string");
        rightHistory.setIsBuiltin(1);
        rightHistory.setChangeType("CREATE");
        when(sysConfigHistoryMapper.selectByIdAndConfigId(7L, 1L)).thenReturn(rightHistory);

        ConfigDiffSummary result = configService.diffConfig(1L, 9L, 7L);

        Assertions.assertEquals(7L, result.compareHistoryId());
        Assertions.assertFalse(result.compareWithCurrent());
        Assertions.assertEquals("CREATE", result.compareChangeType());
        Assertions.assertTrue(result.items().stream().anyMatch(item -> "configValue".equals(item.field()) && item.changed()));
    }

    private ConfigSaveRequest buildRequest() {
        ConfigSaveRequest request = new ConfigSaveRequest();
        request.setConfigKey("sys.theme.default");
        request.setConfigValue("midnight-blue");
        request.setConfigName("默认主题");
        request.setConfigGroup("UI");
        request.setValueType("string");
        request.setIsBuiltin(1);
        request.setRemark("测试");
        return request;
    }
}

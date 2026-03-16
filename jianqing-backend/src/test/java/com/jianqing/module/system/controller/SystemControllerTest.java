package com.jianqing.module.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianqing.common.exception.GlobalExceptionHandler;
import com.jianqing.module.system.dto.ConfigSaveRequest;
import com.jianqing.module.system.dto.ConfigHistorySummary;
import com.jianqing.module.system.dto.ConfigDiffItem;
import com.jianqing.module.system.dto.ConfigDiffSummary;
import com.jianqing.module.system.dto.ConfigRestorePreviewSummary;
import com.jianqing.module.system.dto.ConfigSummary;
import com.jianqing.module.system.dto.DictDataSaveRequest;
import com.jianqing.module.system.dto.DictDataSummary;
import com.jianqing.module.system.dto.DictOptionItem;
import com.jianqing.module.system.dto.DictTypeSaveRequest;
import com.jianqing.module.system.dto.DictTypeSummary;
import com.jianqing.module.system.service.SystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SystemControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SystemService systemService;

    @InjectMocks
    private SystemController systemController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(systemController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnDictTypes() throws Exception {
        when(systemService.listDictTypes()).thenReturn(List.of(new DictTypeSummary(1L, "用户状态", "sys_user_status", 1, "")));

        mockMvc.perform(get("/api/system/dict-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].dictType").value("sys_user_status"));
    }

    @Test
    void shouldCreateDictType() throws Exception {
        when(systemService.createDictType(any(DictTypeSaveRequest.class)))
                .thenReturn(new DictTypeSummary(2L, "用户状态", "sys_user_status", 1, ""));

        mockMvc.perform(post("/api/system/dict-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildDictTypeRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(2));
    }

    @Test
    void shouldReturnDictDataByType() throws Exception {
        when(systemService.listDictData("sys_user_status"))
                .thenReturn(List.of(new DictDataSummary(3L, "sys_user_status", "启用", "1", "success", "", 1, 1, "")));

        mockMvc.perform(get("/api/system/dict-data").param("dictType", "sys_user_status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].label").value("启用"));
    }

    @Test
    void shouldReturnDictOptions() throws Exception {
        when(systemService.listDictOptions("sys_user_status"))
                .thenReturn(List.of(new DictOptionItem("启用", "1", "success", "")));

        mockMvc.perform(get("/api/system/dict-options/sys_user_status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].value").value("1"));
    }

    @Test
    void shouldDeleteDictData() throws Exception {
        mockMvc.perform(post("/api/system/dict-data/9/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(systemService).deleteDictData(9L);
    }

    @Test
    void shouldReturnConfigs() throws Exception {
        when(systemService.listConfigs())
                .thenReturn(List.of(new ConfigSummary(1L, "sys.theme.default", "midnight-blue", "默认主题", "UI", "string", 1, "")));

        mockMvc.perform(get("/api/system/configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].configKey").value("sys.theme.default"));
    }

    @Test
    void shouldCreateConfig() throws Exception {
        when(systemService.createConfig(any(ConfigSaveRequest.class)))
                .thenReturn(new ConfigSummary(2L, "sys.theme.default", "midnight-blue", "默认主题", "UI", "string", 1, ""));

        mockMvc.perform(post("/api/system/configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildConfigRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(2));
    }

    @Test
    void shouldReturnConfigHistory() throws Exception {
        when(systemService.listConfigHistory(1L))
                .thenReturn(List.of(new ConfigHistorySummary(9L, 1L, "sys.theme.default", "默认主题", "UI", "midnight-blue", "string", 1, "UPDATE", "更新参数", "2026-03-13T13:00:00")));

        mockMvc.perform(get("/api/system/configs/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].changeType").value("UPDATE"));
    }

    @Test
    void shouldReturnDeletedConfigHistory() throws Exception {
        when(systemService.listDeletedConfigHistory())
                .thenReturn(List.of(new ConfigHistorySummary(11L, 3L, "sys.login.captcha.enabled", "登录验证码开关", "AUTH", "false", "boolean", 0, "DELETE", "删除参数", "2026-03-13T13:00:00")));

        mockMvc.perform(get("/api/system/configs/deleted/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].changeType").value("DELETE"));
    }

    @Test
    void shouldPreviewDeletedConfigRestore() throws Exception {
        when(systemService.previewDeletedConfigRestore(11L))
                .thenReturn(new ConfigRestorePreviewSummary(11L, "sys.login.captcha.enabled", "登录验证码开关",
                        "AUTH", "false", "boolean", 0, "删除参数", "2026-03-13T13:00:00",
                        true, 9L, "登录验证码开关-当前",
                        List.of(new ConfigDiffItem("configValue", "参数值", "true", "false", true))));

        mockMvc.perform(get("/api/system/configs/deleted/history/11/preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.historyId").value(11))
                .andExpect(jsonPath("$.data.configKey").value("sys.login.captcha.enabled"))
                .andExpect(jsonPath("$.data.keyOccupied").value(true));
    }

    @Test
    void shouldRollbackConfig() throws Exception {
        when(systemService.rollbackConfig(1L, 9L))
                .thenReturn(new ConfigSummary(1L, "sys.theme.default", "midnight-blue", "默认主题", "UI", "string", 1, ""));

        mockMvc.perform(post("/api/system/configs/1/history/9/rollback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.configValue").value("midnight-blue"));
    }

    @Test
    void shouldRestoreDeletedConfig() throws Exception {
        when(systemService.restoreDeletedConfig(11L))
                .thenReturn(new ConfigSummary(8L, "sys.login.captcha.enabled", "false", "登录验证码开关", "AUTH", "boolean", 0, "已从删除历史#11恢复"));

        mockMvc.perform(post("/api/system/configs/history/11/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(8))
                .andExpect(jsonPath("$.data.configKey").value("sys.login.captcha.enabled"));
    }

    @Test
    void shouldReturnConfigDiff() throws Exception {
        when(systemService.diffConfig(1L, 9L, null))
                .thenReturn(new ConfigDiffSummary(1L, 9L, null, "sys.theme.default", "UPDATE", "2026-03-13T13:00:00",
                        null, null, true,
                        List.of(new ConfigDiffItem("configValue", "参数值", "light", "midnight-blue", true))));

        mockMvc.perform(get("/api/system/configs/1/history/9/diff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].field").value("configValue"))
                .andExpect(jsonPath("$.data.items[0].changed").value(true));
    }

    @Test
    void shouldReturnConfigDiffBetweenHistories() throws Exception {
        when(systemService.diffConfig(1L, 9L, 7L))
                .thenReturn(new ConfigDiffSummary(1L, 9L, 7L, "sys.theme.default", "UPDATE", "2026-03-13T13:00:00",
                        "CREATE", "2026-03-13T12:00:00", false,
                        List.of(new ConfigDiffItem("configValue", "参数值", "light", "midnight-blue", true))));

        mockMvc.perform(get("/api/system/configs/1/history/9/diff").param("compareHistoryId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.compareHistoryId").value(7))
                .andExpect(jsonPath("$.data.compareWithCurrent").value(false));
    }

    private DictTypeSaveRequest buildDictTypeRequest() {
        DictTypeSaveRequest request = new DictTypeSaveRequest();
        request.setDictName("用户状态");
        request.setDictType("sys_user_status");
        request.setStatus(1);
        request.setRemark("");
        return request;
    }

    private ConfigSaveRequest buildConfigRequest() {
        ConfigSaveRequest request = new ConfigSaveRequest();
        request.setConfigKey("sys.theme.default");
        request.setConfigValue("midnight-blue");
        request.setConfigName("默认主题");
        request.setConfigGroup("UI");
        request.setValueType("string");
        request.setIsBuiltin(1);
        request.setRemark("");
        return request;
    }

    @SuppressWarnings("unused")
    private DictDataSaveRequest buildDictDataRequest() {
        DictDataSaveRequest request = new DictDataSaveRequest();
        request.setDictType("sys_user_status");
        request.setLabel("启用");
        request.setValue("1");
        request.setSortNo(1);
        request.setStatus(1);
        return request;
    }
}

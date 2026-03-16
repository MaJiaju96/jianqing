package com.jianqing.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jianqing.module.system.dto.ConfigSaveRequest;
import com.jianqing.module.system.dto.ConfigHistorySummary;
import com.jianqing.module.system.dto.ConfigDiffSummary;
import com.jianqing.module.system.dto.ConfigRestorePreviewSummary;
import com.jianqing.module.system.dto.ConfigSummary;
import com.jianqing.module.system.entity.SysConfig;

import java.util.List;

public interface ConfigService extends IService<SysConfig> {

    List<ConfigSummary> listConfigs();

    ConfigSummary createConfig(ConfigSaveRequest request);

    ConfigSummary updateConfig(Long id, ConfigSaveRequest request);

    void deleteConfig(Long id);

    List<ConfigHistorySummary> listConfigHistory(Long configId);

    List<ConfigHistorySummary> listDeletedConfigHistory();

    ConfigRestorePreviewSummary previewDeletedConfigRestore(Long historyId);

    ConfigSummary rollbackConfig(Long configId, Long historyId);

    ConfigSummary restoreDeletedConfig(Long historyId);

    ConfigDiffSummary diffConfig(Long configId, Long historyId, Long compareHistoryId);
}

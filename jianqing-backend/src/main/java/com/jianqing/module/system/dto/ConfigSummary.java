package com.jianqing.module.system.dto;

public record ConfigSummary(Long id, String configKey, String configValue, String configName, String configGroup,
                            String valueType, Integer isBuiltin, String remark) {
}

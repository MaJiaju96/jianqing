package com.jianqing.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ConfigSaveRequest {

    @NotBlank(message = "参数键不能为空")
    private String configKey;
    @NotBlank(message = "参数值不能为空")
    private String configValue;
    @NotBlank(message = "参数名称不能为空")
    private String configName;
    @NotBlank(message = "参数分组不能为空")
    private String configGroup;
    @NotBlank(message = "值类型不能为空")
    private String valueType;
    @NotNull(message = "内置状态不能为空")
    private Integer isBuiltin;
    private String remark;

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getValueType() {
        return valueType;
    }

    public String getConfigGroup() {
        return configGroup;
    }

    public void setConfigGroup(String configGroup) {
        this.configGroup = configGroup;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Integer getIsBuiltin() {
        return isBuiltin;
    }

    public void setIsBuiltin(Integer isBuiltin) {
        this.isBuiltin = isBuiltin;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

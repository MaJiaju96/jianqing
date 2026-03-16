package com.jianqing.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("jq_sys_config_history")
public class SysConfigHistory {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("config_id")
    private Long configId;
    @TableField("config_key")
    private String configKey;
    @TableField("config_name")
    private String configName;
    @TableField("config_group")
    private String configGroup;
    @TableField("config_value")
    private String configValue;
    @TableField("value_type")
    private String valueType;
    @TableField("is_builtin")
    private Integer isBuiltin;
    @TableField("change_type")
    private String changeType;
    @TableField("change_note")
    private String changeNote;
    @TableField("created_by")
    private Long createdBy;
    @TableField("updated_by")
    private Long updatedBy;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }
    public String getConfigGroup() { return configGroup; }
    public void setConfigGroup(String configGroup) { this.configGroup = configGroup; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public String getValueType() { return valueType; }
    public void setValueType(String valueType) { this.valueType = valueType; }
    public Integer getIsBuiltin() { return isBuiltin; }
    public void setIsBuiltin(Integer isBuiltin) { this.isBuiltin = isBuiltin; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public String getChangeNote() { return changeNote; }
    public void setChangeNote(String changeNote) { this.changeNote = changeNote; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

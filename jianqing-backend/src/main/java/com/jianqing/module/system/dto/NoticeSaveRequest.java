package com.jianqing.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class NoticeSaveRequest {

    @NotBlank(message = "通知标题不能为空")
    private String title;
    @NotBlank(message = "通知内容不能为空")
    private String content;
    @NotBlank(message = "通知级别不能为空")
    private String level;
    @NotBlank(message = "发布方式不能为空")
    private String publishMode;
    private LocalDateTime scheduledAt;
    @NotNull(message = "弹窗开关不能为空")
    private Integer popupEnabled;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    @NotBlank(message = "目标类型不能为空")
    private String targetType;
    private List<Long> targetIds;
    private String remark;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getPublishMode() {
        return publishMode;
    }

    public void setPublishMode(String publishMode) {
        this.publishMode = publishMode;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Integer getPopupEnabled() {
        return popupEnabled;
    }

    public void setPopupEnabled(Integer popupEnabled) {
        this.popupEnabled = popupEnabled;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public List<Long> getTargetIds() {
        return targetIds;
    }

    public void setTargetIds(List<Long> targetIds) {
        this.targetIds = targetIds;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

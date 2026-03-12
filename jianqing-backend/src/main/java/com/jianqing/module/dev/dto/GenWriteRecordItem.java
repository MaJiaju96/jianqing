package com.jianqing.module.dev.dto;

import java.time.LocalDateTime;

public class GenWriteRecordItem {

    private String markerId;
    private String tableName;
    private String moduleName;
    private String businessName;
    private String className;
    private String permPrefix;
    private String username;
    private LocalDateTime createdAt;

    public String getMarkerId() {
        return markerId;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPermPrefix() {
        return permPrefix;
    }

    public void setPermPrefix(String permPrefix) {
        this.permPrefix = permPrefix;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

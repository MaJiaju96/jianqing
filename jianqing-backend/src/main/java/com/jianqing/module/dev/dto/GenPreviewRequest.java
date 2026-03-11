package com.jianqing.module.dev.dto;

import jakarta.validation.constraints.NotBlank;

public class GenPreviewRequest {

    @NotBlank(message = "表名不能为空")
    private String tableName;
    @NotBlank(message = "模块名不能为空")
    private String moduleName;
    @NotBlank(message = "业务名不能为空")
    private String businessName;
    @NotBlank(message = "实体名不能为空")
    private String className;
    @NotBlank(message = "权限前缀不能为空")
    private String permPrefix;

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
}

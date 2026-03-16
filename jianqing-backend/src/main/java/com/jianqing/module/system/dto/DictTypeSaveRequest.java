package com.jianqing.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DictTypeSaveRequest {

    @NotBlank(message = "字典名称不能为空")
    private String dictName;
    @NotBlank(message = "字典类型不能为空")
    private String dictType;
    @NotNull(message = "状态不能为空")
    private Integer status;
    private String remark;

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public String getDictType() {
        return dictType;
    }

    public void setDictType(String dictType) {
        this.dictType = dictType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

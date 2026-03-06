package com.jianqing.module.system.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class IdListRequest {

    @NotNull(message = "ID 列表不能为空")
    private List<Long> ids;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}

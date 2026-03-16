package com.jianqing.module.system.dto;

public record DictDataSummary(Long id, String dictType, String label, String value,
                              String colorType, String cssClass, Integer sortNo,
                              Integer status, String remark) {
}

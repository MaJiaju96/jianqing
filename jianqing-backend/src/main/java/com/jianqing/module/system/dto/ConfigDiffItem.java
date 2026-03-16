package com.jianqing.module.system.dto;

public record ConfigDiffItem(String field, String fieldLabel, String currentValue, String historyValue, Boolean changed) {
}

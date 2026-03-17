package com.jianqing.module.system.dto;

import java.util.List;

public record RoleSummary(Long id, String roleName, String roleCode, Integer dataScope, Integer status,
                          List<Long> customDeptIds) {
}

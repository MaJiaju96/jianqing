package com.jianqing.module.system.dto;

public record UserSummary(Long id,
                          String username,
                          String nickname,
                          String realName,
                          String mobile,
                          String email,
                          Long deptId,
                          Integer status) {
}

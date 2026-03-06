package com.jianqing.module.audit.dto;

import java.time.LocalDateTime;

public record OperLogView(Long id,
                          String traceId,
                          Long userId,
                          String username,
                          String moduleName,
                          String bizType,
                          String requestUri,
                          String requestIp,
                          Integer status,
                          String errorMsg,
                          Integer costMs,
                          LocalDateTime createdAt) {
}

package com.jianqing.module.audit.dto;

import java.time.LocalDateTime;

public record LoginLogView(Long id,
                           Long userId,
                           String username,
                           String loginType,
                           String loginIp,
                           String userAgent,
                           Integer status,
                           String msg,
                           LocalDateTime createdAt) {
}

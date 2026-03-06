package com.jianqing.module.audit.controller;

import com.jianqing.common.api.ApiResponse;
import com.jianqing.module.audit.dto.LoginLogView;
import com.jianqing.module.audit.dto.OperLogView;
import com.jianqing.module.audit.dto.PageResult;
import com.jianqing.module.audit.service.AuditLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogService auditLogService;

    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/oper-logs")
    public ApiResponse<PageResult<OperLogView>> operLogs(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "20") int size,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) Integer status) {
        return ApiResponse.success(auditLogService.pageOperLogs(page, size, keyword, status));
    }

    @GetMapping("/login-logs")
    public ApiResponse<PageResult<LoginLogView>> loginLogs(@RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "20") int size,
                                                            @RequestParam(required = false) String keyword,
                                                            @RequestParam(required = false) Integer status,
                                                            @RequestParam(required = false) String loginType) {
        return ApiResponse.success(auditLogService.pageLoginLogs(page, size, keyword, status, loginType));
    }
}

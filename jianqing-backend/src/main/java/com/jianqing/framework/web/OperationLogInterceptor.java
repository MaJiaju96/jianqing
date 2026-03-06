package com.jianqing.framework.web;

import com.jianqing.framework.security.SecurityUtils;
import com.jianqing.module.audit.entity.SysOperLog;
import com.jianqing.module.audit.service.AuditLogService;
import com.jianqing.module.system.service.SystemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class OperationLogInterceptor implements HandlerInterceptor {

    private static final String ATTR_START = "jq_req_start";
    private static final String ATTR_TRACE = "jq_trace_id";

    private final AuditLogService auditLogService;
    private final SystemService systemService;

    public OperationLogInterceptor(AuditLogService auditLogService, SystemService systemService) {
        this.auditLogService = auditLogService;
        this.systemService = systemService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(ATTR_START, System.currentTimeMillis());
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        request.setAttribute(ATTR_TRACE, traceId);
        response.setHeader("X-Trace-Id", traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/") || "GET".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        String username = SecurityUtils.currentUsername();
        Long userId = username == null ? 0L : systemService.findUserIdByUsername(username);

        long start = request.getAttribute(ATTR_START) instanceof Long value ? value : System.currentTimeMillis();
        int cost = (int) Math.max(0, System.currentTimeMillis() - start);

        SysOperLog log = new SysOperLog();
        log.setTraceId(String.valueOf(request.getAttribute(ATTR_TRACE)));
        log.setUserId(userId == null ? 0L : userId);
        log.setUsername(username == null ? "" : username);
        log.setModuleName(resolveModule(uri));
        log.setBizType(request.getMethod());
        log.setMethod(handler.getClass().getSimpleName());
        log.setRequestUri(uri);
        log.setRequestIp(clientIp(request));
        log.setRequestParam(request.getQueryString());
        log.setResponseData(null);
        log.setStatus(ex == null && response.getStatus() < 500 ? 1 : 0);
        log.setErrorMsg(ex == null ? "" : ex.getMessage());
        log.setCostMs(cost);
        auditLogService.saveOperationLog(log);
    }

    private String resolveModule(String uri) {
        String[] parts = uri.split("/");
        if (parts.length >= 3) {
            return parts[2];
        }
        return "unknown";
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

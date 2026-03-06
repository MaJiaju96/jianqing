package com.jianqing.module.audit.service;

import com.jianqing.module.audit.dto.LoginLogView;
import com.jianqing.module.audit.dto.OperLogView;
import com.jianqing.module.audit.dto.PageResult;
import com.jianqing.module.audit.entity.SysLoginLog;
import com.jianqing.module.audit.entity.SysOperLog;
import com.jianqing.module.audit.mapper.SysOperLogMapper;
import com.jianqing.module.audit.service.LoginLogService;
import com.jianqing.module.audit.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private SysOperLogMapper sysOperLogMapper;

    @Mock
    private LoginLogService loginLogService;

    @Test
    void shouldPageOperLogsAndMapResult() {
        AuditLogServiceImpl auditLogService = new AuditLogServiceImpl(sysOperLogMapper, loginLogService);
        SysOperLog operLog = buildOperLog();
        when(sysOperLogMapper.selectCount(any())).thenReturn(5L);
        when(sysOperLogMapper.selectList(any())).thenReturn(List.of(operLog));

        PageResult<OperLogView> result = auditLogService.pageOperLogs(0, 999, null, null);

        Assertions.assertEquals(1L, result.page());
        Assertions.assertEquals(100L, result.size());
        Assertions.assertEquals(5L, result.total());
        Assertions.assertEquals(1, result.records().size());
        Assertions.assertEquals("admin", result.records().get(0).username());
        Assertions.assertEquals("/api/system/users", result.records().get(0).requestUri());
    }

    @Test
    void shouldHandleOperLogFiltersWithEmptyResult() {
        AuditLogServiceImpl auditLogService = new AuditLogServiceImpl(sysOperLogMapper, loginLogService);
        when(sysOperLogMapper.selectCount(any())).thenReturn(0L);
        when(sysOperLogMapper.selectList(any())).thenReturn(List.of());

        PageResult<OperLogView> result = auditLogService.pageOperLogs(2, 20, "admin", 1);

        Assertions.assertEquals(2L, result.page());
        Assertions.assertEquals(20L, result.size());
        Assertions.assertEquals(0L, result.total());
        Assertions.assertTrue(result.records().isEmpty());
        verify(sysOperLogMapper).selectCount(any());
        verify(sysOperLogMapper).selectList(any());
    }

    @Test
    void shouldPageLoginLogsAndMapResult() {
        AuditLogServiceImpl auditLogService = new AuditLogServiceImpl(sysOperLogMapper, loginLogService);
        SysLoginLog loginLog = buildLoginLog();
        when(loginLogService.pageLoginLogs(1, 20, null, null, null))
                .thenReturn(new PageResult<>(1, 20, 3, List.of(new LoginLogView(
                        loginLog.getId(),
                        loginLog.getUserId(),
                        loginLog.getUsername(),
                        loginLog.getLoginType(),
                        loginLog.getLoginIp(),
                        loginLog.getUserAgent(),
                        loginLog.getStatus(),
                        loginLog.getMsg(),
                        loginLog.getCreatedAt()
                ))));

        PageResult<LoginLogView> result = auditLogService.pageLoginLogs(1, 20, null, null, null);

        Assertions.assertEquals(1L, result.page());
        Assertions.assertEquals(20L, result.size());
        Assertions.assertEquals(3L, result.total());
        Assertions.assertEquals(1, result.records().size());
        Assertions.assertEquals("alice", result.records().get(0).username());
        Assertions.assertEquals("password", result.records().get(0).loginType());
    }

    @Test
    void shouldHandleLoginLogFiltersWithEmptyResult() {
        AuditLogServiceImpl auditLogService = new AuditLogServiceImpl(sysOperLogMapper, loginLogService);
        when(loginLogService.pageLoginLogs(1, 10, "alice", 0, "password"))
                .thenReturn(new PageResult<>(1, 10, 0, List.of()));

        PageResult<LoginLogView> result = auditLogService.pageLoginLogs(1, 10, "alice", 0, "password");

        Assertions.assertEquals(1L, result.page());
        Assertions.assertEquals(10L, result.size());
        Assertions.assertEquals(0L, result.total());
        Assertions.assertTrue(result.records().isEmpty());
        verify(loginLogService).pageLoginLogs(1, 10, "alice", 0, "password");
    }

    private SysOperLog buildOperLog() {
        SysOperLog operLog = new SysOperLog();
        operLog.setId(1L);
        operLog.setUsername("admin");
        operLog.setModuleName("system");
        operLog.setBizType("query");
        operLog.setRequestUri("/api/system/users");
        operLog.setRequestIp("127.0.0.1");
        operLog.setStatus(1);
        operLog.setCostMs(12);
        operLog.setCreatedAt(LocalDateTime.now());
        return operLog;
    }

    private SysLoginLog buildLoginLog() {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setId(1L);
        loginLog.setUsername("alice");
        loginLog.setLoginType("password");
        loginLog.setLoginIp("127.0.0.1");
        loginLog.setUserAgent("Chrome");
        loginLog.setStatus(1);
        loginLog.setMsg("登录成功");
        loginLog.setCreatedAt(LocalDateTime.now());
        return loginLog;
    }
}

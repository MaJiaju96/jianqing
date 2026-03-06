package com.jianqing.module.audit.controller;

import com.jianqing.common.exception.GlobalExceptionHandler;
import com.jianqing.module.audit.dto.LoginLogView;
import com.jianqing.module.audit.dto.OperLogView;
import com.jianqing.module.audit.dto.PageResult;
import com.jianqing.module.audit.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuditController auditController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(auditController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnOperLogsWithFilters() throws Exception {
        OperLogView operLogView = new OperLogView(
                1L,
                "trace-1",
                1L,
                "admin",
                "system",
                "query",
                "/api/system/users",
                "127.0.0.1",
                1,
                null,
                10,
                LocalDateTime.of(2026, 3, 6, 12, 0)
        );
        when(auditLogService.pageOperLogs(2, 10, "admin", 1))
                .thenReturn(new PageResult<>(2, 10, 1, List.of(operLogView)));

        mockMvc.perform(get("/api/audit/oper-logs")
                        .param("page", "2")
                        .param("size", "10")
                        .param("keyword", "admin")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.records[0].username").value("admin"));

        verify(auditLogService).pageOperLogs(2, 10, "admin", 1);
    }

    @Test
    void shouldUseDefaultParamsForLoginLogs() throws Exception {
        LoginLogView loginLogView = new LoginLogView(
                1L,
                1L,
                "alice",
                "password",
                "127.0.0.1",
                "Chrome",
                1,
                "登录成功",
                LocalDateTime.of(2026, 3, 6, 12, 10)
        );
        when(auditLogService.pageLoginLogs(1, 20, null, null, null))
                .thenReturn(new PageResult<>(1, 20, 1, List.of(loginLogView)));

        mockMvc.perform(get("/api/audit/login-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.records[0].loginType").value("password"));

        verify(auditLogService).pageLoginLogs(1, 20, null, null, null);
    }
}

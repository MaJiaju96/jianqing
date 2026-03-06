package com.jianqing.module.audit.service;

import com.jianqing.module.audit.dto.LoginLogView;
import com.jianqing.module.audit.dto.OperLogView;
import com.jianqing.module.audit.dto.PageResult;

public interface AuditLogService {

    PageResult<OperLogView> pageOperLogs(int page, int size, String keyword, Integer status);

    PageResult<LoginLogView> pageLoginLogs(int page, int size, String keyword, Integer status, String loginType);
}

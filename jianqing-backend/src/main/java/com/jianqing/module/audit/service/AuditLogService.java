package com.jianqing.module.audit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jianqing.module.audit.dto.LoginLogView;
import com.jianqing.module.audit.dto.OperLogView;
import com.jianqing.module.audit.dto.PageResult;
import com.jianqing.module.audit.entity.SysLoginLog;
import com.jianqing.module.audit.entity.SysOperLog;

/**
 * 审计服务接口，负责操作日志写入与审计日志查询聚合。
 */
public interface AuditLogService extends IService<SysOperLog> {

    /**
     * 查询操作日志分页。
     */
    PageResult<OperLogView> pageOperLogs(int page, int size, String keyword, Integer status);

    /**
     * 查询登录日志分页。
     */
    PageResult<LoginLogView> pageLoginLogs(int page, int size, String keyword, Integer status, String loginType);

    /**
     * 写入操作日志。
     */
    void saveOperationLog(SysOperLog log);

    /**
     * 写入登录日志。
     */
    void saveLoginLog(SysLoginLog log);
}

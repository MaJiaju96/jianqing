package com.jianqing.module.audit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jianqing.module.audit.dto.LoginLogView;
import com.jianqing.module.audit.dto.PageResult;
import com.jianqing.module.audit.entity.SysLoginLog;

/**
 * 登录日志服务接口，负责登录日志写入与筛选分页查询。
 */
public interface LoginLogService extends IService<SysLoginLog> {

    /**
     * 按条件分页查询登录日志。
     */
    PageResult<LoginLogView> pageLoginLogs(int page, int size, String keyword, Integer status, String loginType);

    /**
     * 写入登录日志。
     */
    void saveLoginLog(SysLoginLog log);
}

package com.jianqing.module.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jianqing.module.audit.dto.LoginLogView;
import com.jianqing.module.audit.dto.OperLogView;
import com.jianqing.module.audit.dto.PageResult;
import com.jianqing.module.audit.entity.SysLoginLog;
import com.jianqing.module.audit.entity.SysOperLog;
import com.jianqing.module.audit.mapper.SysOperLogMapper;
import com.jianqing.module.audit.service.AuditLogService;
import com.jianqing.module.audit.service.LoginLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AuditLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements AuditLogService {

    private final LoginLogService loginLogService;

    public AuditLogServiceImpl(SysOperLogMapper sysOperLogMapper, LoginLogService loginLogService) {
        this.baseMapper = sysOperLogMapper;
        this.loginLogService = loginLogService;
    }

    @Override
    public PageResult<OperLogView> pageOperLogs(int page, int size, String keyword, Integer status) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        int offset = (safePage - 1) * safeSize;

        Long total = baseMapper.selectCount(buildOperLogQuery(keyword, status));
        List<SysOperLog> rows = baseMapper.selectList(buildOperLogQuery(keyword, status)
                .orderByDesc(SysOperLog::getId)
                .last("limit " + offset + "," + safeSize));
        List<OperLogView> data = rows.stream().map(item -> new OperLogView(item.getId(), item.getTraceId(), item.getUserId(),
                item.getUsername(), item.getModuleName(), item.getBizType(), item.getRequestUri(), item.getRequestIp(),
                item.getStatus(), item.getErrorMsg(), item.getCostMs(), item.getCreatedAt())).toList();
        return new PageResult<>(safePage, safeSize, total == null ? 0 : total, data);
    }

    @Override
    public PageResult<LoginLogView> pageLoginLogs(int page, int size, String keyword, Integer status, String loginType) {
        return loginLogService.pageLoginLogs(page, size, keyword, status, loginType);
    }

    @Override
    public void saveOperationLog(SysOperLog log) {
        if (log == null) {
            return;
        }
        baseMapper.insert(log);
    }

    @Override
    public void saveLoginLog(SysLoginLog log) {
        loginLogService.saveLoginLog(log);
    }

    private LambdaQueryWrapper<SysOperLog> buildOperLogQuery(String keyword, Integer status) {
        LambdaQueryWrapper<SysOperLog> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(SysOperLog::getUsername, keyword)
                    .or()
                    .like(SysOperLog::getModuleName, keyword)
                    .or()
                    .like(SysOperLog::getRequestUri, keyword));
        }
        if (status != null) {
            queryWrapper.eq(SysOperLog::getStatus, status);
        }
        return queryWrapper;
    }

}

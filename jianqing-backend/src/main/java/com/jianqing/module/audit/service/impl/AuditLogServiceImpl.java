package com.jianqing.module.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianqing.module.audit.dto.LoginLogView;
import com.jianqing.module.audit.dto.OperLogView;
import com.jianqing.module.audit.dto.PageResult;
import com.jianqing.module.audit.entity.SysLoginLog;
import com.jianqing.module.audit.entity.SysOperLog;
import com.jianqing.module.audit.mapper.SysLoginLogMapper;
import com.jianqing.module.audit.mapper.SysOperLogMapper;
import com.jianqing.module.audit.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final SysOperLogMapper sysOperLogMapper;
    private final SysLoginLogMapper sysLoginLogMapper;

    public AuditLogServiceImpl(SysOperLogMapper sysOperLogMapper, SysLoginLogMapper sysLoginLogMapper) {
        this.sysOperLogMapper = sysOperLogMapper;
        this.sysLoginLogMapper = sysLoginLogMapper;
    }

    @Override
    public PageResult<OperLogView> pageOperLogs(int page, int size, String keyword, Integer status) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        int offset = (safePage - 1) * safeSize;

        Long total = sysOperLogMapper.selectCount(buildOperLogQuery(keyword, status));
        List<SysOperLog> rows = sysOperLogMapper.selectList(buildOperLogQuery(keyword, status)
                .orderByDesc(SysOperLog::getId)
                .last("limit " + offset + "," + safeSize));
        List<OperLogView> data = rows.stream().map(item -> new OperLogView(item.getId(), item.getTraceId(), item.getUserId(),
                item.getUsername(), item.getModuleName(), item.getBizType(), item.getRequestUri(), item.getRequestIp(),
                item.getStatus(), item.getErrorMsg(), item.getCostMs(), item.getCreatedAt())).toList();
        return new PageResult<>(safePage, safeSize, total == null ? 0 : total, data);
    }

    @Override
    public PageResult<LoginLogView> pageLoginLogs(int page, int size, String keyword, Integer status, String loginType) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        int offset = (safePage - 1) * safeSize;

        Long total = sysLoginLogMapper.selectCount(buildLoginLogQuery(keyword, status, loginType));
        List<SysLoginLog> rows = sysLoginLogMapper.selectList(buildLoginLogQuery(keyword, status, loginType)
                .orderByDesc(SysLoginLog::getId)
                .last("limit " + offset + "," + safeSize));
        List<LoginLogView> data = rows.stream().map(item -> new LoginLogView(item.getId(), item.getUserId(),
                item.getUsername(), item.getLoginType(), item.getLoginIp(), item.getUserAgent(),
                item.getStatus(), item.getMsg(), item.getCreatedAt())).toList();
        return new PageResult<>(safePage, safeSize, total == null ? 0 : total, data);
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

    private LambdaQueryWrapper<SysLoginLog> buildLoginLogQuery(String keyword, Integer status, String loginType) {
        LambdaQueryWrapper<SysLoginLog> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(SysLoginLog::getUsername, keyword)
                    .or()
                    .like(SysLoginLog::getLoginIp, keyword)
                    .or()
                    .like(SysLoginLog::getMsg, keyword));
        }
        if (status != null) {
            queryWrapper.eq(SysLoginLog::getStatus, status);
        }
        if (StringUtils.hasText(loginType)) {
            queryWrapper.eq(SysLoginLog::getLoginType, loginType);
        }
        return queryWrapper;
    }
}

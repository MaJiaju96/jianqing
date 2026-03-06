package com.jianqing.module.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jianqing.module.audit.dto.LoginLogView;
import com.jianqing.module.audit.dto.PageResult;
import com.jianqing.module.audit.entity.SysLoginLog;
import com.jianqing.module.audit.mapper.SysLoginLogMapper;
import com.jianqing.module.audit.service.LoginLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class LoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements LoginLogService {

    @Override
    public PageResult<LoginLogView> pageLoginLogs(int page, int size, String keyword, Integer status, String loginType) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        int offset = (safePage - 1) * safeSize;
        Long total = baseMapper.selectCount(buildLoginLogQuery(keyword, status, loginType));
        List<SysLoginLog> rows = baseMapper.selectList(buildLoginLogQuery(keyword, status, loginType)
                .orderByDesc(SysLoginLog::getId)
                .last("limit " + offset + "," + safeSize));
        List<LoginLogView> data = rows.stream().map(item -> new LoginLogView(item.getId(), item.getUserId(),
                item.getUsername(), item.getLoginType(), item.getLoginIp(), item.getUserAgent(),
                item.getStatus(), item.getMsg(), item.getCreatedAt())).toList();
        return new PageResult<>(safePage, safeSize, total == null ? 0 : total, data);
    }

    @Override
    public void saveLoginLog(SysLoginLog log) {
        if (log == null) {
            return;
        }
        baseMapper.insert(log);
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

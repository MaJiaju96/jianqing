package com.jianqing.module.system.service.impl;

import com.jianqing.module.system.dto.MyNoticeDetailSummary;
import com.jianqing.module.system.dto.MyNoticeSummary;
import com.jianqing.module.system.dto.NoticeDetailSummary;
import com.jianqing.module.system.dto.NoticeInboxRow;
import com.jianqing.module.system.dto.NoticeSaveRequest;
import com.jianqing.module.system.dto.NoticeSummary;
import com.jianqing.module.system.entity.SysNotice;
import com.jianqing.module.system.entity.SysNoticeUser;
import com.jianqing.module.system.mapper.SysNoticeMapper;
import com.jianqing.module.system.mapper.SysNoticeTargetMapper;
import com.jianqing.module.system.mapper.SysNoticeUserMapper;
import com.jianqing.module.system.mapper.SysUserMapper;
import com.jianqing.module.system.service.NoticeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class NoticeServiceImpl implements NoticeService {

    private static final Set<String> ALLOWED_LEVELS = Set.of("NORMAL", "IMPORTANT", "URGENT");
    private static final Set<String> ALLOWED_PUBLISH_MODES = Set.of("IMMEDIATE", "SCHEDULED");
    private static final Set<String> ALLOWED_TARGET_TYPES = Set.of("ALL", "ROLE", "DEPT", "USER");
    private static final Set<String> EDITABLE_STATUSES = Set.of("DRAFT", "PENDING", "CANCELLED");
    private static final int DEFAULT_LATEST_LIMIT = 5;

    private final SysNoticeMapper sysNoticeMapper;
    private final SysNoticeTargetMapper sysNoticeTargetMapper;
    private final SysNoticeUserMapper sysNoticeUserMapper;
    private final SysUserMapper sysUserMapper;

    public NoticeServiceImpl(SysNoticeMapper sysNoticeMapper,
                             SysNoticeTargetMapper sysNoticeTargetMapper,
                             SysNoticeUserMapper sysNoticeUserMapper,
                             SysUserMapper sysUserMapper) {
        this.sysNoticeMapper = sysNoticeMapper;
        this.sysNoticeTargetMapper = sysNoticeTargetMapper;
        this.sysNoticeUserMapper = sysNoticeUserMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public List<NoticeSummary> listNotices() {
        return sysNoticeMapper.selectAllNotices().stream().map(this::toNoticeSummary).toList();
    }

    @Override
    public NoticeDetailSummary getNoticeDetail(Long id) {
        return toNoticeDetail(getNoticeOrThrow(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoticeDetailSummary createNotice(NoticeSaveRequest request) {
        SysNotice notice = new SysNotice();
        fillNotice(notice, request);
        notice.setStatus(resolveDraftStatus(notice));
        notice.setPublishedAt(null);
        sysNoticeMapper.insert(notice);
        replaceTargets(notice.getId(), notice.getTargetType(), normalizeTargetIds(notice.getTargetType(), request.getTargetIds()));
        return toNoticeDetail(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoticeDetailSummary updateNotice(Long id, NoticeSaveRequest request) {
        SysNotice notice = getNoticeOrThrow(id);
        if (!EDITABLE_STATUSES.contains(safeUpper(notice.getStatus()))) {
            throw new IllegalArgumentException("已发布通知不允许编辑");
        }
        fillNotice(notice, request);
        notice.setStatus(resolveDraftStatus(notice));
        notice.setPublishedAt(null);
        sysNoticeMapper.updateById(notice);
        replaceTargets(notice.getId(), notice.getTargetType(), normalizeTargetIds(notice.getTargetType(), request.getTargetIds()));
        return toNoticeDetail(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoticeDetailSummary publishNotice(Long id) {
        SysNotice notice = getNoticeOrThrow(id);
        if ("PUBLISHED".equals(safeUpper(notice.getStatus()))) {
            throw new IllegalArgumentException("通知已发布");
        }
        if ("SCHEDULED".equals(safeUpper(notice.getPublishMode()))
                && notice.getScheduledAt() != null
                && notice.getScheduledAt().isAfter(LocalDateTime.now())) {
            notice.setStatus("PENDING");
            sysNoticeMapper.updateById(notice);
            return toNoticeDetail(notice);
        }
        publishNow(notice, LocalDateTime.now());
        return toNoticeDetail(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoticeDetailSummary cancelNotice(Long id) {
        SysNotice notice = getNoticeOrThrow(id);
        if ("PUBLISHED".equals(safeUpper(notice.getStatus()))) {
            throw new IllegalArgumentException("已发布通知不支持取消");
        }
        notice.setStatus("CANCELLED");
        sysNoticeMapper.updateById(notice);
        return toNoticeDetail(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id) {
        SysNotice notice = getNoticeOrThrow(id);
        if ("PUBLISHED".equals(safeUpper(notice.getStatus()))) {
            throw new IllegalArgumentException("已发布通知不允许删除");
        }
        sysNoticeTargetMapper.deleteByNoticeId(notice.getId());
        sysNoticeUserMapper.deleteByNoticeId(notice.getId());
        sysNoticeMapper.deleteById(notice.getId());
    }

    @Override
    public List<MyNoticeSummary> listMyNotices(Long userId) {
        return sysNoticeUserMapper.selectMyNotices(userId).stream().map(this::toMyNoticeSummary).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyNoticeDetailSummary getMyNoticeDetail(Long userId, Long noticeId) {
        NoticeInboxRow row = getMyNoticeOrThrow(userId, noticeId);
        if (row.getReadStatus() == null || row.getReadStatus() == 0) {
            sysNoticeUserMapper.markRead(userId, noticeId, LocalDateTime.now());
            row.setReadStatus(1);
            row.setReadAt(LocalDateTime.now());
        }
        return toMyNoticeDetail(row);
    }

    @Override
    public Long countUnreadNotices(Long userId) {
        Long count = sysNoticeUserMapper.countUnreadByUserId(userId);
        return count == null ? 0L : count;
    }

    @Override
    public List<MyNoticeSummary> listLatestNotices(Long userId, int limit) {
        int safeLimit = limit <= 0 ? DEFAULT_LATEST_LIMIT : Math.min(limit, 20);
        return sysNoticeUserMapper.selectLatestByUserId(userId, safeLimit).stream().map(this::toMyNoticeSummary).toList();
    }

    @Override
    public List<MyNoticeSummary> listPopupCandidates(Long userId, int limit) {
        int safeLimit = limit <= 0 ? DEFAULT_LATEST_LIMIT : Math.min(limit, 20);
        return sysNoticeUserMapper.selectPopupCandidates(userId, LocalDateTime.now(), safeLimit).stream()
                .map(this::toMyNoticeSummary)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markNoticeRead(Long userId, Long noticeId) {
        getMyNoticeOrThrow(userId, noticeId);
        sysNoticeUserMapper.markRead(userId, noticeId, LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllNoticesRead(Long userId) {
        sysNoticeUserMapper.markAllRead(userId, LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishPendingNotices() {
        LocalDateTime now = LocalDateTime.now();
        for (SysNotice notice : sysNoticeMapper.selectPendingPublishNotices(now)) {
            publishNow(notice, now);
        }
    }

    private void publishNow(SysNotice notice, LocalDateTime publishTime) {
        List<Long> recipientUserIds = resolveRecipientUserIds(notice.getTargetType(), sysNoticeTargetMapper.selectTargetIdsByNoticeId(notice.getId()));
        sysNoticeUserMapper.deleteByNoticeId(notice.getId());
        if (!recipientUserIds.isEmpty()) {
            List<SysNoticeUser> items = new ArrayList<>();
            for (Long userId : recipientUserIds) {
                SysNoticeUser item = new SysNoticeUser();
                item.setNoticeId(notice.getId());
                item.setUserId(userId);
                item.setReadStatus(0);
                items.add(item);
            }
            sysNoticeUserMapper.batchInsert(items);
        }
        notice.setStatus("PUBLISHED");
        notice.setPublishedAt(publishTime);
        sysNoticeMapper.updateById(notice);
    }

    private List<Long> resolveRecipientUserIds(String targetType, List<Long> targetIds) {
        String normalizedTargetType = safeUpper(targetType);
        if ("ALL".equals(normalizedTargetType)) {
            return deduplicate(sysUserMapper.selectAllActiveUserIds());
        }
        if (targetIds == null || targetIds.isEmpty()) {
            return List.of();
        }
        return switch (normalizedTargetType) {
            case "ROLE" -> deduplicate(sysUserMapper.selectActiveUserIdsByRoleIds(targetIds));
            case "DEPT" -> deduplicate(sysUserMapper.selectActiveUserIdsByDeptIds(targetIds));
            case "USER" -> deduplicate(sysUserMapper.selectExistingActiveUserIds(targetIds));
            default -> throw new IllegalArgumentException("通知目标类型不支持");
        };
    }

    private List<Long> deduplicate(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(userIds));
    }

    private void fillNotice(SysNotice notice, NoticeSaveRequest request) {
        String title = safeRequiredText(request.getTitle(), "通知标题不能为空");
        String content = safeRequiredText(request.getContent(), "通知内容不能为空");
        String level = normalizeEnum(request.getLevel(), ALLOWED_LEVELS, "通知级别仅支持 NORMAL/IMPORTANT/URGENT");
        String publishMode = normalizeEnum(request.getPublishMode(), ALLOWED_PUBLISH_MODES, "发布方式仅支持 IMMEDIATE/SCHEDULED");
        String targetType = normalizeEnum(request.getTargetType(), ALLOWED_TARGET_TYPES, "目标类型仅支持 ALL/ROLE/DEPT/USER");
        validateSchedule(publishMode, request.getScheduledAt());
        validateValidity(request.getValidFrom(), request.getValidTo());
        validatePopupEnabled(request.getPopupEnabled());
        validateTargetIds(targetType, request.getTargetIds());
        notice.setTitle(title);
        notice.setContent(content);
        notice.setLevel(level);
        notice.setPublishMode(publishMode);
        notice.setScheduledAt("SCHEDULED".equals(publishMode) ? request.getScheduledAt() : null);
        notice.setPopupEnabled(request.getPopupEnabled());
        notice.setValidFrom(request.getValidFrom());
        notice.setValidTo(request.getValidTo());
        notice.setTargetType(targetType);
        notice.setRemark(safeText(request.getRemark()));
    }

    private String resolveDraftStatus(SysNotice notice) {
        if ("SCHEDULED".equals(safeUpper(notice.getPublishMode()))) {
            return "PENDING";
        }
        return "DRAFT";
    }

    private void validateSchedule(String publishMode, LocalDateTime scheduledAt) {
        if ("SCHEDULED".equals(publishMode)) {
            if (scheduledAt == null) {
                throw new IllegalArgumentException("定时发布必须填写计划时间");
            }
            return;
        }
        if (scheduledAt != null) {
            throw new IllegalArgumentException("立即发布不允许填写计划时间");
        }
    }

    private void validateValidity(LocalDateTime validFrom, LocalDateTime validTo) {
        if (validFrom != null && validTo != null && validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException("通知有效期结束时间不能早于开始时间");
        }
    }

    private void validatePopupEnabled(Integer popupEnabled) {
        if (popupEnabled == null || (popupEnabled != 0 && popupEnabled != 1)) {
            throw new IllegalArgumentException("弹窗开关仅支持 0/1");
        }
    }

    private void validateTargetIds(String targetType, List<Long> targetIds) {
        if ("ALL".equals(targetType)) {
            return;
        }
        if (targetIds == null || targetIds.stream().filter(id -> id != null && id > 0).findAny().isEmpty()) {
            throw new IllegalArgumentException("当前目标类型必须至少选择一个目标");
        }
    }

    private List<Long> normalizeTargetIds(String targetType, List<Long> targetIds) {
        if ("ALL".equals(targetType)) {
            return List.of(0L);
        }
        LinkedHashSet<Long> normalized = new LinkedHashSet<>();
        if (targetIds != null) {
            for (Long targetId : targetIds) {
                if (targetId != null && targetId > 0) {
                    normalized.add(targetId);
                }
            }
        }
        return new ArrayList<>(normalized);
    }

    private void replaceTargets(Long noticeId, String targetType, List<Long> targetIds) {
        sysNoticeTargetMapper.deleteByNoticeId(noticeId);
        sysNoticeTargetMapper.batchInsert(noticeId, targetType, targetIds);
    }

    private SysNotice getNoticeOrThrow(Long id) {
        SysNotice notice = sysNoticeMapper.selectById(id);
        if (notice == null) {
            throw new IllegalArgumentException("通知不存在");
        }
        return notice;
    }

    private NoticeInboxRow getMyNoticeOrThrow(Long userId, Long noticeId) {
        NoticeInboxRow row = sysNoticeUserMapper.selectMyNoticeById(userId, noticeId);
        if (row == null) {
            throw new IllegalArgumentException("消息不存在");
        }
        return row;
    }

    private NoticeSummary toNoticeSummary(SysNotice notice) {
        return new NoticeSummary(notice.getId(), notice.getTitle(), notice.getLevel(), notice.getPublishMode(),
                notice.getTargetType(), notice.getPopupEnabled(), notice.getStatus(), timeText(notice.getScheduledAt()),
                timeText(notice.getPublishedAt()), timeText(notice.getValidFrom()), timeText(notice.getValidTo()), notice.getRemark());
    }

    private NoticeDetailSummary toNoticeDetail(SysNotice notice) {
        List<Long> targetIds = sysNoticeTargetMapper.selectTargetIdsByNoticeId(notice.getId());
        if ("ALL".equals(safeUpper(notice.getTargetType()))) {
            targetIds = List.of();
        }
        return new NoticeDetailSummary(notice.getId(), notice.getTitle(), notice.getContent(), notice.getLevel(),
                notice.getPublishMode(), notice.getTargetType(), targetIds,
                notice.getPopupEnabled(), notice.getStatus(), timeText(notice.getScheduledAt()), timeText(notice.getPublishedAt()),
                timeText(notice.getValidFrom()), timeText(notice.getValidTo()), notice.getRemark());
    }

    private MyNoticeSummary toMyNoticeSummary(NoticeInboxRow row) {
        return new MyNoticeSummary(row.getNoticeId(), row.getTitle(), row.getLevel(), row.getPopupEnabled(),
                row.getReadStatus(), timeText(row.getPublishedAt()), timeText(row.getReadAt()));
    }

    private MyNoticeDetailSummary toMyNoticeDetail(NoticeInboxRow row) {
        return new MyNoticeDetailSummary(row.getNoticeId(), row.getTitle(), row.getContent(), row.getLevel(),
                row.getPopupEnabled(), row.getReadStatus(), timeText(row.getPublishedAt()), timeText(row.getReadAt()),
                timeText(row.getValidFrom()), timeText(row.getValidTo()));
    }

    private String normalizeEnum(String value, Set<String> allowedValues, String message) {
        String normalized = safeUpper(value);
        if (!allowedValues.contains(normalized)) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private String safeRequiredText(String text, String message) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return text.trim();
    }

    private String safeText(String text) {
        return text == null ? "" : text.trim();
    }

    private String safeUpper(String text) {
        return safeText(text).toUpperCase(Locale.ROOT);
    }

    private String timeText(LocalDateTime time) {
        return time == null ? "" : time.toString();
    }
}

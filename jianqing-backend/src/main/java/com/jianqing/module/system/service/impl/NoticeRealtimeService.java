package com.jianqing.module.system.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianqing.module.system.dto.MyNoticeSummary;
import com.jianqing.module.system.dto.NoticeInboxRow;
import com.jianqing.module.system.dto.NoticeRealtimeSummary;
import com.jianqing.module.system.mapper.SysNoticeUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NoticeRealtimeService {

    private static final Logger log = LoggerFactory.getLogger(NoticeRealtimeService.class);
    private static final long SSE_TIMEOUT_MILLIS = 0L;
    private static final int DEFAULT_LATEST_LIMIT = 5;

    private final SysNoticeUserMapper sysNoticeUserMapper;
    private final ObjectMapper objectMapper;
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitterStore = new ConcurrentHashMap<>();

    public NoticeRealtimeService(SysNoticeUserMapper sysNoticeUserMapper, ObjectMapper objectMapper) {
        this.sysNoticeUserMapper = sysNoticeUserMapper;
        this.objectMapper = objectMapper;
    }

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        emitterStore.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(error -> removeEmitter(userId, emitter));
        sendSnapshot(userId, emitter);
        return emitter;
    }

    public void publishUserNoticeState(Long userId) {
        if (userId == null || userId <= 0) {
            return;
        }
        CopyOnWriteArrayList<SseEmitter> emitters = emitterStore.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        NoticeRealtimeSummary snapshot = buildSnapshot(userId);
        for (SseEmitter emitter : emitters) {
            sendNoticeEvent(userId, emitter, snapshot);
        }
    }

    public void publishUserNoticeStates(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : new LinkedHashSet<>(userIds)) {
            publishUserNoticeState(userId);
        }
    }

    @Scheduled(fixedDelay = 25000L)
    public void heartbeat() {
        for (Map.Entry<Long, CopyOnWriteArrayList<SseEmitter>> entry : emitterStore.entrySet()) {
            Long userId = entry.getKey();
            for (SseEmitter emitter : entry.getValue()) {
                try {
                    emitter.send(SseEmitter.event().name("ping").comment("keep-alive"));
                } catch (IOException ex) {
                    log.debug("notice sse heartbeat failed, userId={}", userId, ex);
                    removeEmitter(userId, emitter);
                }
            }
        }
    }

    private void sendSnapshot(Long userId, SseEmitter emitter) {
        sendNoticeEvent(userId, emitter, buildSnapshot(userId));
    }

    private NoticeRealtimeSummary buildSnapshot(Long userId) {
        Long unreadCount = sysNoticeUserMapper.countUnreadByUserId(userId);
        List<MyNoticeSummary> latestNotices = sysNoticeUserMapper.selectLatestByUserId(userId, DEFAULT_LATEST_LIMIT).stream()
                .map(this::toMyNoticeSummary)
                .toList();
        return new NoticeRealtimeSummary(unreadCount == null ? 0L : unreadCount, latestNotices);
    }

    private void sendNoticeEvent(Long userId, SseEmitter emitter, NoticeRealtimeSummary snapshot) {
        try {
            emitter.send(SseEmitter.event()
                    .name("notice-sync")
                    .data(objectMapper.writeValueAsString(snapshot), MediaType.APPLICATION_JSON));
        } catch (JsonProcessingException ex) {
            log.warn("notice snapshot serialization failed, userId={}", userId, ex);
            removeEmitter(userId, emitter);
        } catch (IOException ex) {
            log.debug("notice sse send failed, userId={}", userId, ex);
            removeEmitter(userId, emitter);
        }
    }

    private MyNoticeSummary toMyNoticeSummary(NoticeInboxRow row) {
        return new MyNoticeSummary(row.getNoticeId(), row.getTitle(), row.getContent(), row.getLevel(), row.getPopupEnabled(),
                row.getReadStatus(), timeText(row.getPublishedAt()), timeText(row.getReadAt()));
    }

    private String timeText(LocalDateTime time) {
        return time == null ? null : time.toString().replace('T', ' ');
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = emitterStore.get(userId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emitterStore.remove(userId);
        }
    }
}

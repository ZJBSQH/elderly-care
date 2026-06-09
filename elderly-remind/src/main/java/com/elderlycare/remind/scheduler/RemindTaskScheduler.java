package com.elderlycare.remind.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.remind.entity.Notification;
import com.elderlycare.remind.entity.RemindTask;
import com.elderlycare.remind.mapper.NotificationMapper;
import com.elderlycare.remind.service.RemindService;
import com.elderlycare.remind.websocket.NotifyWebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 提醒任务定时调度器
 * 每分钟扫描一次，匹配当前时间的提醒任务并推送通知
 *
 * @author 郑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemindTaskScheduler {

    /** 提醒服务 */
    private final RemindService remindService;
    /** 通知记录 Mapper */
    private final NotificationMapper notificationMapper;

    /**
     * 每分钟执行一次，检查并触发提醒任务
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndTriggerTasks() {
        try {
            LocalTime now = LocalTime.now().withSecond(0).withNano(0);
            LocalDate today = LocalDate.now();

            // 获取所有当天需要执行的任务
            var tasks = remindService.getAllTodayTasksRaw();
            if (tasks == null || tasks.isEmpty()) {
                return;
            }

            // 筛选当前分钟匹配的任务
            var matchedTasks = tasks.stream()
                    .filter(task -> task.getRemindTime() != null)
                    .filter(task -> {
                        LocalTime taskTime = task.getRemindTime().withSecond(0).withNano(0);
                        return taskTime.equals(now);
                    }).toList();

            if (matchedTasks.isEmpty()) {
                return;
            }

            log.info("当前时间 {} 匹配到 {} 条提醒任务", now, matchedTasks.size());

            for (RemindTask task : matchedTasks) {
                triggerTask(task, today);
            }
        } catch (Exception e) {
            log.error("定时扫描提醒任务异常", e);
        }
    }

    /**
     * 触发单个提醒任务：生成通知记录并推送 WebSocket 消息
     */
    private void triggerTask(RemindTask task, LocalDate today) {
        try {
            // 防止重复通知：查询同一任务在最近 1 分钟内是否已发送过通知
            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
            LambdaQueryWrapper<Notification> duplicateWrapper = new LambdaQueryWrapper<>();
            duplicateWrapper.eq(Notification::getTaskId, task.getId())
                    .ge(Notification::getSendTime, oneMinuteAgo);
            Long duplicateCount = notificationMapper.selectCount(duplicateWrapper);
            if (duplicateCount != null && duplicateCount > 0) {
                log.info("任务 ID: {} 在最近 1 分钟内已有通知记录，跳过重复推送", task.getId());
                return;
            }

            // 构建通知记录
            Notification notification = new Notification();
            notification.setTaskId(task.getId());
            notification.setUserId(task.getUserId());
            notification.setElderId(task.getElderId());
            notification.setTitle(task.getTitle());
            notification.setContent(task.getContent() != null ? task.getContent() : task.getTitle());
            notification.setNotifyType(task.getRemindType());
            notification.setSendTime(LocalDateTime.now());
            notification.setReadStatus(0);
            notification.setStatus(1);
            notification.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(notification);
            log.info("通知记录已生成, 任务 ID: {}, 通知 ID: {}", task.getId(), notification.getId());

            // 推送 WebSocket 消息
            String userId = String.valueOf(task.getUserId());
            var notifyMessage = NotifyWebSocket.NotifyMessage.success(
                    java.util.Map.of(
                            "taskId", task.getId(),
                            "notificationId", notification.getId(),
                            "title", task.getTitle(),
                            "content", notification.getContent(),
                            "remindType", task.getRemindType(),
                            "remindTime", task.getRemindTime().toString(),
                            "needVoice", task.getNeedVoice(),
                            "needPopup", task.getNeedPopup(),
                            "voiceText", task.getVoiceText()
                    )
            );
            NotifyWebSocket.sendToUser(userId, notifyMessage);
            log.info("WebSocket 通知已推送给用户 {}", userId);

        } catch (Exception e) {
            log.error("触发提醒任务失败, 任务 ID: {}", task.getId(), e);
        }
    }
}

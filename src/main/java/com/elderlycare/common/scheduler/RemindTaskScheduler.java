package com.elderlycare.common.scheduler;

import cn.hutool.core.date.DateUtil;
import com.elderlycare.mapper.UserMapper;
import com.elderlycare.pojo.entity.User;
import com.elderlycare.pojo.vo.NotificationVO;
import com.elderlycare.pojo.vo.RemindTaskVO;
import com.elderlycare.service.RemindPushService;
import com.elderlycare.service.RemindService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提醒任务调度器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemindTaskScheduler {

    private final RemindService remindService;
    private final RemindPushService remindPushService;
    private final UserMapper userMapper;



    /**
     * 每分钟检查一次是否需要触发提醒
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndTriggerRemindTasks() {
        log.info("开始检查提醒任务...");

        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        LocalDate currentDate = now.toLocalDate();

        try {
            // 查询所有用户今日的提醒任务
            List<RemindTaskVO> todayTasks = remindService.getAllTodayTasks().getData();

            if (todayTasks == null || todayTasks.isEmpty()) {
                log.debug("今日暂无提醒任务");
                return;
            }
            List<Integer> userIds = todayTasks.stream()
                    .map(RemindTaskVO::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Integer, String> userNameMap = userIds.stream()
                    .map(userId -> {
                        User user = userMapper.selectById(userId);
                        return user != null ? Map.entry(userId, user.getName()) : Map.entry(userId, "未知用户");
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            for (RemindTaskVO task : todayTasks) {

                if (task.getStatus() != 1) {
                    continue;
                }

                if (!task.getRemindDate().equals(currentDate)) {
                    continue;
                }

                LocalTime taskTime = task.getRemindTime();
                if (taskTime.getHour() == currentTime.getHour() &&
                        taskTime.getMinute() == currentTime.getMinute()) {

                    String userName = userNameMap.getOrDefault(task.getUserId(), "未知用户");

                    log.info("触发提醒任务，taskId: {}, title: {}, 用户: {}(ID:{})",
                            task.getId(), task.getTitle(), userName, task.getUserId());

                    NotificationVO notification = triggerTaskInternal(task.getId());

                    if (notification != null) {
                        remindPushService.pushNotification(task.getUserId(), task, notification);

                        if (Boolean.TRUE.equals(task.getNeedVoice())) {
                            remindPushService.triggerVoiceBroadcast(task);
                        }

                        if (Boolean.TRUE.equals(task.getNeedPopup())) {
                            remindPushService.triggerPopupAlert(task);
                        }
                    }
                }
            }

            log.info("提醒任务检查完成");
        } catch (Exception e) {
            log.error("检查提醒任务失败", e);
        }
    }

    /**
     * 触发提醒任务（内部方法）
     */
    private NotificationVO triggerTaskInternal(Integer taskId) {
        try {
            var result = remindService.triggerTask(taskId);
            return result.getData();
        } catch (Exception e) {
            log.error("触发提醒任务失败，taskId: {}", taskId, e);
            return null;
        }
    }

    /**
     * 每小时检查一次重复任务
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkRepeatTasks() {
        log.info("检查重复提醒任务...");

        try {
            List<RemindTaskVO> allTasks = remindService.listTasks(null).getData();

            if (allTasks == null || allTasks.isEmpty()) {
                return;
            }

            for (RemindTaskVO task : allTasks) {
                if (task.getStatus() != 1 || task.getRepeatCycle() == null || task.getRepeatCycle() == 0) {
                    continue;
                }

                if (task.getEndDate() != null && LocalDate.now().isAfter(task.getEndDate())) {
                    log.info("重复任务已过期，taskId: {}", task.getId());
                }
            }

        } catch (Exception e) {
            log.error("检查重复任务失败", e);
        }
    }
}


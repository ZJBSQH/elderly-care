package com.elderlycare.controller.test;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.vo.RemindTaskVO;
import com.elderlycare.pojo.vo.NotificationVO;
import com.elderlycare.service.RemindPushService;
import com.elderlycare.service.RemindService;
import com.elderlycare.common.websocket.NotifyWebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 提醒推送控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/remind/push")
public class RemindPushController {

    private final RemindPushService remindPushService;
    private final RemindService remindService;

    /**
     * 手动触发提醒并推送（测试用）
     */
    @PostMapping("/trigger/{taskId}")
    public Result<Void> manualTrigger(@PathVariable Integer taskId) {
        try {
            log.info("手动触发提醒，taskId: {}", taskId);

            // 查询任务详情
            var taskResult = remindService.getTaskById(taskId);
            if (taskResult.getCode() != 200 || taskResult.getData() == null) {
                return Result.error("提醒任务不存在");
            }

            RemindTaskVO task = taskResult.getData();

            // 触发任务并创建通知记录
            var triggerResult = remindService.triggerTask(taskId);
            if (triggerResult.getCode() != 200 || triggerResult.getData() == null) {
                return Result.error("触发提醒失败");
            }

            NotificationVO notification = triggerResult.getData();

            // 推送通知到前端
            remindPushService.pushNotification(task.getUserId(), task, notification);

            // 根据配置触发语音播报和弹窗
            if (Boolean.TRUE.equals(task.getNeedVoice())) {
                remindPushService.triggerVoiceBroadcast(task);
            }

            if (Boolean.TRUE.equals(task.getNeedPopup())) {
                remindPushService.triggerPopupAlert(task);
            }

            log.info("手动触发提醒成功，taskId: {}", taskId);
            return Result.success(null);
        } catch (Exception e) {
            log.error("手动触发提醒失败", e);
            return Result.error("触发提醒失败：" + e.getMessage());
        }
    }

    /**
     * 测试 WebSocket 推送
     */
    @PostMapping("/test/{userId}")
    public Result<Map<String, Object>> testPush(@PathVariable Integer userId) {
        try {
            log.info("发送测试推送消息，userId: {}", userId);

            Map<String, Object> testData = new HashMap<>();
            testData.put("title", "测试提醒");
            testData.put("content", "这是一条测试提醒消息");
            testData.put("remindType", 1);
            testData.put("action", "test");
            testData.put("sendTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            NotifyWebSocket.NotifyMessage message = new NotifyWebSocket.NotifyMessage(
                    200,
                    "测试消息",
                    testData
            );

            NotifyWebSocket.sendToUser(String.valueOf(userId), message);

            log.info("测试推送消息发送成功，userId: {}", userId);
            return Result.success(testData);
        } catch (Exception e) {
            log.error("发送测试推送消息失败", e);
            return Result.error("发送测试消息失败：" + e.getMessage());
        }
    }
}


package com.elderlycare.service.impl;

import com.elderlycare.pojo.vo.RemindTaskVO;
import com.elderlycare.pojo.vo.NotificationVO;
import com.elderlycare.service.RemindPushService;
import com.elderlycare.common.websocket.NotifyWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 提醒推送服务实现
 */
@Slf4j
@Service
public class RemindPushServiceImpl implements RemindPushService {

    @Override
    public void pushNotification(Integer userId, RemindTaskVO task, NotificationVO notification) {
        log.info("准备推送提醒通知，userId: {}, taskId: {}", userId, task.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("taskId", task.getId());
        data.put("medicineId", task.getMedicineId());
        data.put("notificationId", notification.getId());
        data.put("title", task.getTitle());
        data.put("content", task.getContent());
        data.put("remindType", task.getRemindType());
        data.put("remindTime", task.getRemindTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        data.put("needVoice", task.getNeedVoice());
        data.put("needPopup", task.getNeedPopup());
        data.put("voiceText", task.getVoiceText());
        data.put("sendTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        NotifyWebSocket.NotifyMessage message = new NotifyWebSocket.NotifyMessage(
                200,
                "提醒通知",
                data
        );

        NotifyWebSocket.sendToUser(String.valueOf(userId), message);
        log.info("提醒通知已推送，userId: {}, taskId: {}", userId, task.getId());
    }

    @Override
    public void triggerVoiceBroadcast(RemindTaskVO task) {
        log.info("触发语音播报，taskId: {}, voiceText: {}", task.getId(), task.getVoiceText());
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", task.getId());
        data.put("medicineId", task.getMedicineId());
        data.put("voiceText", task.getVoiceText() != null ? task.getVoiceText() : task.getContent());
        data.put("action", "voice_broadcast");

        NotifyWebSocket.NotifyMessage message = new NotifyWebSocket.NotifyMessage(
                200,
                "语音播报",
                data
        );

        NotifyWebSocket.sendToUser(String.valueOf(task.getUserId()), message);
    }

    @Override
    public void triggerPopupAlert(RemindTaskVO task) {
        log.info("触发弹窗提醒，taskId: {}, title: {}", task.getId(), task.getTitle());
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", task.getId());
        data.put("title", task.getTitle());
        data.put("content", task.getContent());
        data.put("remindType", task.getRemindType());
        data.put("action", "popup_alert");

        NotifyWebSocket.NotifyMessage message = new NotifyWebSocket.NotifyMessage(
                200,
                "弹窗提醒",
                data
        );

        NotifyWebSocket.sendToUser(String.valueOf(task.getUserId()), message);
    }
}


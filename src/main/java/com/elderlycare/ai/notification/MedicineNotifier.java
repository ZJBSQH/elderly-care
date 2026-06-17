// src/main/java/com/elderlycare/ai/notification/MedicineNotifier.java
package com.elderlycare.ai.notification;

import com.elderlycare.common.websocket.NotifyWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MedicineNotifier {

    public void sendMedicineReminder(Integer elderId, String message) {
        sendMessage(elderId, message, "medicine_reminder");
    }

    public void sendMissedMedicineIntervention(Integer elderId, String message) {
        sendMessage(elderId, message, "missed_medicine_intervention");
    }

    private void sendMessage(Integer elderId, String message, String action) {
        Map<String, Object> data = new HashMap<>();
        data.put("action", action);
        data.put("message", message);

        NotifyWebSocket.NotifyMessage wsMessage = new NotifyWebSocket.NotifyMessage(200, "AI 助手", data);
        NotifyWebSocket.sendToUser(String.valueOf(elderId), wsMessage);
        log.info("AI 消息已推送给老人 ID: {}, 动作: {}, 内容: {}", elderId, action, message);
    }
}
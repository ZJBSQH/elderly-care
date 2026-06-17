package com.elderlycare.service;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

// AI助手服务接口
public interface AIAssistantService {
    // 处理药物问题
    Flux<ServerSentEvent<String>> processMedicineQuestionStream(Integer elderId, String userInput);
    // 处理其他问题
    void initiateMedicineReminder(Integer elderId);
    // 处理未提醒的药品
    void handleMissedMedicine(Integer elderId, Integer taskId);
    // 检查并提醒今天要提醒的药品
    void checkAndRemindTodayMedicines();
}
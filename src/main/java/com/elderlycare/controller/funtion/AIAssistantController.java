
package com.elderlycare.controller.funtion;

import com.elderlycare.common.Result;
import com.elderlycare.service.AIAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai-assistant")
public class AIAssistantController {

    private final AIAssistantService aiAssistantService;

    // 处理问诊问题 - 流式输出
    @PostMapping(value = "/medicine/question", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> askMedicineQuestion(
            @RequestParam Integer elderId,
            @RequestBody Map<String, String> request) {
        String question = request.get("question");
        log.info("收到老人 ID: {} 的用药咨询请求（流式）", elderId);
        return aiAssistantService.processMedicineQuestionStream(elderId, question);
    }

    // 提醒老人吃药 - 通过WebSocket推送，保持原有方式
    @PostMapping("/medicine/remind/{elderId}")
    public Result<Void> initiateReminder(@PathVariable Integer elderId) {
        log.info("收到老人 ID: {} 的用药提醒请求", elderId);
        aiAssistantService.initiateMedicineReminder(elderId);
        return Result.success(null);
    }

    // 获取今日用药建议 - 流式输出
    @GetMapping(value = "/medicine/today-advice/{elderId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getTodayAdvice(@PathVariable Integer elderId) {
        log.info("获取老人 ID: {} 的今日用药建议（流式）", elderId);
        String defaultQuestion = "请根据我的用药计划，给出今天的用药建议和注意事项";
        return aiAssistantService.processMedicineQuestionStream(elderId, defaultQuestion);
    }

    // 漏服干预 - 通过WebSocket推送，保持原有方式
    @PostMapping("/medicine/missed")
    public Result<Void> handleMissed(
            @RequestParam Integer elderId,
            @RequestParam Integer taskId) {
        log.info("处理老人 ID: {} 的任务 ID: {} 的漏服干预", elderId, taskId);
        aiAssistantService.handleMissedMedicine(elderId, taskId);
        return Result.success(null);
    }
}

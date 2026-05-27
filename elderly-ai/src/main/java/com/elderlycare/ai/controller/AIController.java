package com.elderlycare.ai.controller;

import com.elderlycare.ai.service.AIAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI助手控制器
 * 提供用药相关的AI问答接口，通过SSE流式返回结果
 *
 * @author 郑
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIAssistantService aiAssistantService;

    /**
     * 药品问题AI问答
     * 接收用户问题，返回SSE流式AI回答
     *
     * @param elderId  老人ID
     * @param question 用户问题
     * @return SSE事件流
     */
    @PostMapping(value = "/medicine/question", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> processMedicineQuestion(
            @RequestParam Integer elderId,
            @RequestBody String question) {
        log.info("收到药品AI问题请求，elderId: {}, question: {}", elderId, question);
        return aiAssistantService.processMedicineQuestion(elderId, question);
    }

    /**
     * 今日用药建议
     * 基于老人今日用药数据提供智能用药建议
     *
     * @param elderId 老人ID
     * @return SSE事件流
     */
    @GetMapping(value = "/medicine/today-advice", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getTodayAdvice(@RequestParam Integer elderId) {
        log.info("收到今日用药建议请求，elderId: {}", elderId);
        return aiAssistantService.processMedicineQuestion(elderId, "请根据我今天的用药情况，给我一些用药建议和健康提醒");
    }
}

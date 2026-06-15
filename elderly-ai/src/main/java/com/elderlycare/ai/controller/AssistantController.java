package com.elderlycare.ai.controller;

import com.elderlycare.ai.feign.UserAccessFeignClient;
import com.elderlycare.ai.service.MedicalAssistant;
import com.elderlycare.ai.service.MedicalContextBuilder;
import com.elderlycare.common.core.exception.BaseErrorCode;
import com.elderlycare.common.core.exception.BusinessException;
import com.elderlycare.common.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 助手控制器
 * 提供用药相关的 SSE 流式问答接口。
 */
@Slf4j
@RequestMapping("/ai")
@RestController
@RequiredArgsConstructor
public class AssistantController {

    private final MedicalAssistant assistant;
    private final MedicalContextBuilder contextBuilder;
    private final SecurityUtil securityUtil;
    private final UserAccessFeignClient userAccessFeignClient;

    /**
     * 药品问题 AI 问答，基于当前老人档案构建个性化上下文。
     */
    @PostMapping(value = "/medicine/question", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> processMedicineQuestion(@RequestBody String question) {
        Integer elderId = resolveCurrentElderId();
        log.info("收到药品 AI 问题请求，elderId: {}, question: {}", elderId, question);
        return Flux.defer(() -> {
                    // 核心逻辑：先聚合用药和提醒上下文，再交给大模型流式生成。
                    String context = contextBuilder.buildAndFormat(elderId);
                    String fullMessage = context + "\n\n用户问题: " + question;
                    return assistant.chat(fullMessage);
                })
                .map(token -> ServerSentEvent.<String>builder()
                        .data(token)
                        .build());
    }

    /**
     * 今日用药建议，基于当前老人当天用药数据生成总结。
     */
    @GetMapping(value = "/medicine/today-advice", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getTodayAdvice() {
        Integer elderId = resolveCurrentElderId();
        log.info("收到今日用药建议请求，elderId: {}", elderId);
        return Flux.defer(() -> {
                    // 核心逻辑：根据当前老人档案构建上下文，避免误把 userId 当成 elderId。
                    String context = contextBuilder.buildAndFormat(elderId);
                    String fullMessage = context
                            + "\n\n请根据以上用药数据，给出今日用药总结和健康建议。"
                            + "如发现漏服情况，请说明补服注意事项。";
                    return assistant.chat(fullMessage);
                })
                .map(token -> ServerSentEvent.<String>builder()
                        .data(token)
                        .build());
    }

    /**
     * 获取当前登录老人对应的老人档案 ID。
     */
    private Integer resolveCurrentElderId() {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(BaseErrorCode.UNAUTHORIZED, "请先登录后再使用个性化 AI 问答");
        }
        return userAccessFeignClient.getCurrentElderId().getData();
    }
}

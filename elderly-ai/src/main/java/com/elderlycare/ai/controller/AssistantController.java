package com.elderlycare.ai.controller;

import com.elderlycare.ai.service.MedicalAssistant;
import com.elderlycare.ai.service.MedicalContextBuilder;
import com.elderlycare.common.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * AI 助手控制器
 * <p>
 * 提供用药相关的 AI 问答接口，通过 SSE 流式返回 AI 回答。
 * 每次请求先通过 {@link MedicalContextBuilder} 拉取最新上下文数据，
 * 再交由 {@link MedicalAssistant} 调用大模型生成回答。
 *
 * @author 郑
 */
@Slf4j
@RequestMapping("/ai")
@RestController
@RequiredArgsConstructor
public class AssistantController {

    private final MedicalAssistant assistant;
    private final MedicalContextBuilder contextBuilder;
    private final SecurityUtil securityUtil;


    /**
     * 药品问题AI问答（SSE 流式逐 token 输出）
     * <p>
     * 先在后台线程构建医疗上下文（Feign 阻塞调用），
     * 再通过 LangChain4j 的 streaming-chat-model 逐 token 推送 SSE 事件。
     *
     * @param question 用户问题
     * @return SSE 事件流（逐 token）
     */
    @PostMapping(value = "/medicine/question", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> processMedicineQuestion(
            @RequestBody String question) {
        Integer elderId = securityUtil.getCurrentUserId();
        log.info("收到药品AI问题请求，elderId: {}, question: {}", elderId, question);
        return Flux.defer(() -> {
                    // 1. 构建医疗上下文（阻塞调用，defer 确保订阅时才执行）
                    String context = contextBuilder.buildAndFormat(elderId);
                    String fullMessage = context + "\n\n用户问题: " + question;
                    // 2. 直接返回流式 AI 的 Flux，无需 Mono→Flux 转换
                    return assistant.chat(fullMessage);
                })
                .map(token -> ServerSentEvent.<String>builder()
                        .data(token)
                        .build());
    }

    /**
     * 今日用药建议（SSE 流式逐 token 输出）
     * <p>
     * 基于老人今日用药数据，自动生成用药总结和健康建议。
     * 先从 SecurityContext 中获取当前用户的 elderId，
     * 再构建上下文并流式推送 AI 生成的每个 token。
     *
     * @return SSE 流式 AI 建议（逐 token）
     */
    @GetMapping(value = "/medicine/today-advice",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getTodayAdvice() {
        Integer elderId = securityUtil.getCurrentUserId();
        log.info("收到今日用药建议请求，elderId: {}", elderId);
        return Flux.defer(() -> {
                    // 1. 构建医疗上下文（阻塞调用，defer 确保订阅时才执行）
                    String context = contextBuilder.buildAndFormat(elderId);
                    String fullMessage = context
                            + "\n\n请根据以上用药数据，给出今日用药总结和健康建议。"
                            + "如发现漏服情况，请说明补服注意事项。";
                    // 2. 直接返回流式 AI 的 Flux
                    return assistant.chat(fullMessage);
                })
                .map(token -> ServerSentEvent.<String>builder()
                        .data(token)
                        .build());
    }

    /**
     * 异常处理：当 AI 调用失败时，返回友好的错误提示
     * <p>
     * 该方法是 Spring WebFlux 的异常处理补充，
     * 也可在 GlobalExceptionHandler 中统一处理。
     */
    private Flux<String> errorFallback(String message) {
        return Flux.just("抱歉，" + message + "，请稍后再试。");
    }

}

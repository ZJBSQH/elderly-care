package com.elderlycare.ai.controller;

import com.elderlycare.ai.dto.RagQueryDTO;
import com.elderlycare.ai.service.RagService;
import com.elderlycare.common.security.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * RAG 增强检索生成控制器
 * <p>
 * 在现有普通 AI 问答基础上，增加基于医学知识库的增强问答能力。
 * 用户问题先经过向量检索匹配相关知识片段，再结合个人医疗数据生成更精准的回答。
 *
 * @author 郑
 */
@Slf4j
@RequestMapping("/ai/rag")
@RestController
@RequiredArgsConstructor
public class RagAssistantController {

    private final RagService ragService;
    private final SecurityUtil securityUtil;

    /**
     * RAG 增强问答（SSE 流式逐 token 输出）
     * <p>
     * 1. 用户问题向量化后在知识库中检索相关医学知识
     * 2. 结合用户个人医疗上下文
     * 3. 流式生成增强后的 AI 回答
     * <p>
     * 若知识库检索失败，自动降级为仅使用个人数据的普通问答。
     *
     * @param dto 查询请求（含问题文本和可选的检索参数覆写）
     * @return SSE 事件流（逐 token）
     */
    @PostMapping(value = "/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> askWithRag(@Valid @RequestBody RagQueryDTO dto) {
        Integer elderId = securityUtil.getCurrentUserId();
        // TODO: 测试阶段，未登录时默认使用 elderId=1 的老人数据
        if (elderId == null) {
            elderId = 1;
            log.info("未获取到登录用户，使用默认测试账号 elderId=1");
        }
        log.info("收到 RAG 问答请求，elderId: {}, question: {}", elderId, dto.getQuestion());

        return ragService.askWithRag(elderId, dto.getQuestion())
                .map(token -> ServerSentEvent.<String>builder()
                        .data(token)
                        .build())
                .concatWith(Mono.just(
                        ServerSentEvent.<String>builder()
                                .data("[DONE]")
                                .build()));
    }

    /**
     * RAG 健康知识问答（无需登录）
     * <p>
     * 仅基于医学知识库检索，不关联用户个人数据。
     * 适合未登录用户或家属查询通用健康知识。
     *
     * @param dto 查询请求
     * @return SSE 事件流（逐 token）
     */
    @PostMapping(value = "/health-knowledge", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> askHealthKnowledge(@Valid @RequestBody RagQueryDTO dto) {
        log.info("收到健康知识问答请求，question: {}", dto.getQuestion());
        // 不传 elderId，不注入个人上下文
        return ragService.askWithRag(null, dto.getQuestion())
                .map(token -> ServerSentEvent.<String>builder()
                        .data(token)
                        .build())
                .concatWith(Mono.just(
                        ServerSentEvent.<String>builder()
                                .data("[DONE]")
                                .build()));
    }
}

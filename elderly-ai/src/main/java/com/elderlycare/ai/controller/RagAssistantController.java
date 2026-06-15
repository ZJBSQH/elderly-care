package com.elderlycare.ai.controller;

import com.elderlycare.ai.dto.RagQueryDTO;
import com.elderlycare.ai.feign.UserAccessFeignClient;
import com.elderlycare.ai.service.RagService;
import com.elderlycare.common.core.exception.BaseErrorCode;
import com.elderlycare.common.core.exception.BusinessException;
import com.elderlycare.common.security.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * RAG 增强检索生成控制器
 * 提供个性化问答和公开健康知识问答两个入口。
 */
@Slf4j
@RequestMapping("/ai/rag")
@RestController
@RequiredArgsConstructor
public class RagAssistantController {

    private final RagService ragService;
    private final SecurityUtil securityUtil;
    private final UserAccessFeignClient userAccessFeignClient;

    /**
     * 个性化 RAG 问答，必须登录后才能读取当前用户的个人用药上下文。
     */
    @PostMapping(value = "/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> askWithRag(@Valid @RequestBody RagQueryDTO dto) {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(BaseErrorCode.UNAUTHORIZED, "请先登录后再使用个性化 RAG 问答");
        }
        Integer elderId = userAccessFeignClient.getCurrentElderId().getData();
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
     * 公开健康知识问答，不注入个人数据，允许未登录用户使用。
     */
    @PostMapping(value = "/health-knowledge", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> askHealthKnowledge(@Valid @RequestBody RagQueryDTO dto) {
        log.info("收到健康知识问答请求，question: {}", dto.getQuestion());
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

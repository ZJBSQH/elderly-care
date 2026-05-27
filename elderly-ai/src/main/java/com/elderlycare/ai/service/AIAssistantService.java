package com.elderlycare.ai.service;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * AI助手服务接口
 * 处理药品相关的AI问答，通过SSE流式返回结果
 *
 * @author 郑
 */
public interface AIAssistantService {

    /**
     * 处理药品相关问题，通过SSE流式返回AI回答
     *
     * @param elderId  老人ID
     * @param question 用户问题
     * @return SSE事件流
     */
    Flux<ServerSentEvent<String>> processMedicineQuestion(Integer elderId, String question);
}

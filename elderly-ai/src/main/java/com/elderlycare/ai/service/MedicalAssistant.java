package com.elderlycare.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

@AiService
public interface MedicalAssistant {

    /**
     * 基于上下文数据的智能问答（流式输出）
     * <p>
     * 上下文数据应在调用前由 {@link MedicalContextBuilder} 构建并拼入 userMessage。
     * LangChain4j 检测到返回类型为 Flux 时自动使用 streaming-chat-model，
     * 实现逐 token 的 SSE 流式推送。
     * <p>
     * 本服务为无状态设计——每次请求的用药数据、提醒等上下文均由调用方重新构建，
     * 因此不使用 @MemoryId（无需 ChatMemoryProvider），避免不必要的会话记忆开销。
     *
     * @param userMessage 包含上下文数据和用户问题的完整消息
     * @return 流式 AI 回答（逐 token）
     */
    @SystemMessage("""
            You are a professional medical information assistant.
            Your role is to provide general health and medication information.
            - Do NOT diagnose, prescribe, or provide personal medical advice.
            - Always remind users to consult a licensed healthcare provider for specific concerns.
            - Keep answers concise, factual, and easy to understand.
            """)
    @UserMessage("{{question}}")
    Flux<String> chat(@V("question") String userMessage);

}

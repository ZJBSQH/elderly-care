// src/main/java/com/elderlycare/ai/provider/MedicineAIProvider.java
package com.elderlycare.ai.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * 老人用药AI服务提供
 * 核心功能:用药问题咨询,用要提醒生成,漏服干预提醒
 * 所有功能都通过AI服务提供，AI服务调用失败时，会进行重试，最多重试3次，每次间隔1秒。
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicineAIProvider {

    private final ChatClient chatClient;

    @Value("${ai.timeout:30}")
    private int timeoutSeconds;

    @Value("${ai.max-retries:3}")
    private int maxRetries;

    /**
     * 根据上下文和用户问题生成用药咨询回答（流式）
     * @param context 药品知识库上下文
     * @param question 用户提问
     * @return AI生成的回答流
     */
    public Flux<String> getResponseForQuestionStream(String context, String question) {
        String prompt = buildConsultPrompt(context, question);
        return callAiStream(prompt);
    }

    /**
     * 根据上下文和用户问题生成用药咨询回答
     * @param context 药品知识库上下文
     * @param question 用户提问
     * @return AI生活的回答
     */
    public String getResponseForQuestion(String context, String question) {
        String prompt = buildConsultPrompt(context, question);
        return callAiWithRetry( prompt);
    }



    /**
     * 根据药品知识库生成用药提醒
     * @param context 药品知识库上下文
     * @return 用药提醒
     */

    public String getReminderMessage(String context) {
        String prompt = buildReminderPrompt( context);
        return callAiWithRetry( prompt);
    }

    /**
     * 根据药品知识库生成漏服干预提醒
     * @param context 药品知识库上下文
     * @return 漏服干预提醒
     */
    public String getMissedInterventionMessage(String context) {
        String prompt = buildMissedInterventionPrompt( context);
        return callAiWithRetry( prompt);
    }
    //------------------------提示词构建
    // 构建咨询问题
    private String buildConsultPrompt(String context, String question) {
        return """
                你是一个专门为老年人提供用药咨询的智能助手。请根据以下信息回答用户的问题，
                务必用亲切、易懂的语言。如果问题超出信息范围或涉及具体诊断，
                请强调仅供参考，建议咨询医生。
    
                %s
    
                用户问题: %s
                """.formatted(context, question);
    }
    // 构建提醒问题
    private String buildReminderPrompt(String context) {
        return """
                你是一个专门为老年人提供用药提醒的智能助手。请根据以下信息生成一段 suitable and gentle 的用药提醒，
                语气要温和，适合老人。
    
                %s
                """.formatted(context);
    }

    // 构建漏服干预问题
    private String buildMissedInterventionPrompt(String context) {
        return """
                你是一个专门为老年人提供漏服干预的智能助手。请根据以下信息生成一段 suitable and gentle 的提醒，
                询问老人是否忘记了服药，并根据药品特性给出是否需要补服的初步建议（注意：强调这只是建议，最终需遵医嘱），
                并提醒联系家属或医生。
    
                %s
                """.formatted(context);
    }

    /**
     * 流式调用AI服务
     * @param prompt 提示词
     * @return AI服务返回的答案流
     */
    private Flux<String> callAiStream(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content()
                .doOnError(e -> log.error("AI 流式调用失败: {}", e.getMessage()));
    }

    /**
     * 百炼1.1.2.0不支持retry ，所以使用while循环实现
     * 调用AI服务，并重试
     * @param prompt 提示词
     * @return AI服务返回的答案
     */
    private String callAiWithRetry(String prompt) {
        Exception lastException = null;
        for (int i = 0; i < maxRetries; i++) {
            try {
                return chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();
            } catch (Exception e) {
                lastException = e;
                log.warn("AI 调用失败 (尝试 {}/{}): {}", i + 1, maxRetries, e.getMessage());

                if (i < maxRetries - 1) {
                    try {
                        Thread.sleep(1000L * (i + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("AI 调用被中断", ie);
                    }
                }
            }
        }
        throw new RuntimeException("AI 服务调用失败，已重试 " + maxRetries + " 次", lastException);
    }

}

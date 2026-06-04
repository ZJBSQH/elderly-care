package com.elderlycare.ai.service.impl;

import cn.hutool.json.JSONUtil;
import com.elderlycare.ai.feign.MedicineFeignClient;
import com.elderlycare.ai.feign.RemindFeignClient;
import com.elderlycare.ai.service.AIAssistantService;
import com.elderlycare.common.core.result.Result;
import com.elderlycare.common.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * AI助手服务实现
 * <p>
 * 聚合药品和提醒数据，构建 AI 问答上下文，通过 SSE 流式返回 AI 回答。
 * 当前为模拟实现，后续将接入 Spring AI Alibaba DashScope 实现真正的 AI 问答。
 * <p>
 * 核心流程：
 * <ol>
 *   <li>通过 Feign 并发调取药品清单、今日用药记录、今日提醒任务</li>
 *   <li>每个数据源独立 try-catch，单点故障不影响整体流程</li>
 *   <li>组装上下文 JSON → 生成回答 → 以 SSE 事件流返回</li>
 * </ol>
 *
 * @author 郑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIAssistantServiceImpl implements AIAssistantService {

    /** 药品服务 Feign 客户端，获取药品清单和用药记录 */
    private final MedicineFeignClient medicineFeignClient;

    /** 提醒服务 Feign 客户端，获取今日提醒任务 */
    private final RemindFeignClient remindFeignClient;

    /** 安全工具类，用于获取当前登录用户信息（预留） */
    private final SecurityUtil securityUtil;

    /**
     * 处理药品相关问题
     * <p>
     * 使用 {@link Flux#defer} 延迟执行，确保每次订阅都重新构建上下文数据，
     * 然后将上下文 JSON、AI 回答、结束信号依次以 SSE 事件发送。
     *
     * @param elderId  老人ID
     * @param question 用户问题
     * @return SSE 事件流（context → message → done，异常时返回 error → done）
     */
    @Override
    public Flux<ServerSentEvent<String>> processMedicineQuestion(Integer elderId, String question) {
        log.info("处理药品AI问题，elderId: {}, question: {}", elderId, question);

        // 使用 defer 延迟执行，保证每次订阅都获取最新的上下文数据
        return Flux.defer(() -> {
            try {
                // 1. 聚合各服务数据，构建 AI 上下文
                String context = buildContextData(elderId);

                // 2. 基于上下文生成 AI 回答（当前为模拟实现）
                String answer = generateSimulatedAnswer(question, context);

                // 3. 以 SSE 事件流依次返回：上下文 → 回答 → 结束信号
                return Flux.just(
                        createSseEvent("context", context),
                        createSseEvent("message", answer),
                        createSseEvent("done", "[DONE]")
                );
            } catch (Exception e) {
                // 兜底异常处理：保证即使未预料的异常也能以 SSE 格式通知客户端
                log.error("AI处理异常，elderId: {}", elderId, e);
                return Flux.just(
                        createSseEvent("error", "AI服务处理异常: " + e.getMessage()),
                        createSseEvent("done", "[DONE]")
                );
            }
        });
    }

    /**
     * 构建 AI 上下文数据
     * <p>
     * 依次调用药品服务（药品清单、今日用药记录）和提醒服务（今日提醒任务），
     * 每个 Feign 调用都独立 try-catch，单个服务异常不会中断整体流程，
     * 失败的数据源置为空列表，确保 AI 仍能基于可用数据生成回答。
     *
     * @param elderId 老人ID
     * @return 上下文数据 JSON 字符串
     */
    private String buildContextData(Integer elderId) {
        log.info("构建AI上下文数据，elderId: {}", elderId);

        // 使用 LinkedHashMap 保持数据插入顺序，便于阅读和调试
        Map<String, Object> contextMap = new java.util.LinkedHashMap<>();

        // 数据源1：老人药品清单
        try {
            Result<List<Map<String, Object>>> result = medicineFeignClient.getMedicinesByElderId(elderId);
            List<Map<String, Object>> medicines = result != null ? result.getData() : null;
            contextMap.put("medicines", medicines != null ? medicines : List.of());
            log.info("获取药品列表成功，数量: {}", medicines != null ? medicines.size() : 0);
        } catch (Exception e) {
            log.error("获取药品列表失败，elderId: {}", elderId, e);
            contextMap.put("medicines", List.of());
        }

        // 数据源2：今日用药记录（含服药状态、服用时间等）
        try {
            Result<List<Map<String, Object>>> result = medicineFeignClient.getTodayRecords(elderId);
            List<Map<String, Object>> todayRecords = result != null ? result.getData() : null;
            contextMap.put("todayRecords", todayRecords != null ? todayRecords : List.of());
            log.info("获取今日用药记录成功，数量: {}", todayRecords != null ? todayRecords.size() : 0);
        } catch (Exception e) {
            log.error("获取今日用药记录失败，elderId: {}", elderId, e);
            contextMap.put("todayRecords", List.of());
        }

        // 数据源3：今日提醒任务（用药提醒、健康检查提醒等）
        try {
            Result<List<Map<String, Object>>> result = remindFeignClient.getTodayTasks();
            List<Map<String, Object>> todayTasks = result != null ? result.getData() : null;
            contextMap.put("todayTasks", todayTasks != null ? todayTasks : List.of());
            log.info("获取今日提醒任务成功，数量: {}", todayTasks != null ? todayTasks.size() : 0);
        } catch (Exception e) {
            log.error("获取今日提醒任务失败，elderId: {}", elderId, e);
            contextMap.put("todayTasks", List.of());
        }

        // 将上下文 Map 序列化为 JSON 字符串
        return JSONUtil.toJsonStr(contextMap);
    }

    /**
     * 生成模拟 AI 回答
     * <p>
     * 当前为模拟实现，将用户问题和上下文数据拼接为固定模板的文本回答。
     * 后续将替换为调用 DashScope API，由大模型基于上下文生成个性化用药建议。
     *
     * @param question 用户问题
     * @param context  上下文数据（JSON 格式）
     * @return 模拟的 AI 回答文本
     */
    private String generateSimulatedAnswer(String question, String context) {
        StringBuilder answer = new StringBuilder();
        answer.append("您好，我是您的智能用药助手。\n\n");
        answer.append("您的问题是：").append(question).append("\n\n");
        answer.append("根据系统中的数据，我为您整理如下信息：\n");
        answer.append(context).append("\n\n");
        answer.append("温馨提示：以上信息仅供参考，请遵医嘱按时服药。如有不适，请及时联系医生。");
        return answer.toString();
    }

    /**
     * 创建 SSE 事件对象
     *
     * @param event 事件类型（context / message / done / error）
     * @param data  事件携带的数据
     * @return ServerSentEvent 实例
     */
    private ServerSentEvent<String> createSseEvent(String event, String data) {
        return ServerSentEvent.<String>builder()
                .event(event)
                .data(data)
                .build();
    }
}

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
 * 聚合药品和提醒数据，通过SSE流式返回AI回答（当前为模拟实现，后续接入DashScope）
 *
 * @author 郑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIAssistantServiceImpl implements AIAssistantService {

    private final MedicineFeignClient medicineFeignClient;
    private final RemindFeignClient remindFeignClient;
    private final SecurityUtil securityUtil;

    @Override
    public Flux<ServerSentEvent<String>> processMedicineQuestion(Integer elderId, String question) {
        log.info("处理药品AI问题，elderId: {}, question: {}", elderId, question);

        return Flux.defer(() -> {
            try {
                // 构建上下文数据
                String context = buildContextData(elderId);

                // 构建模拟AI回答
                String answer = generateSimulatedAnswer(question, context);

                // 通过SSE流式返回
                return Flux.just(
                        createSseEvent("context", context),
                        createSseEvent("message", answer),
                        createSseEvent("done", "[DONE]")
                );
            } catch (Exception e) {
                log.error("AI处理异常，elderId: {}", elderId, e);
                return Flux.just(
                        createSseEvent("error", "AI服务处理异常: " + e.getMessage()),
                        createSseEvent("done", "[DONE]")
                );
            }
        });
    }

    /**
     * 构建AI上下文数据
     * 从药品和提醒服务获取相关数据，封装为JSON字符串
     *
     * @param elderId 老人ID
     * @return 上下文数据JSON
     */
    private String buildContextData(Integer elderId) {
        log.info("构建AI上下文数据，elderId: {}", elderId);

        Map<String, Object> contextMap = new java.util.LinkedHashMap<>();

        try {
            Result<List<Map<String, Object>>> result = medicineFeignClient.getMedicinesByElderId(elderId);
            List<Map<String, Object>> medicines = result != null ? result.getData() : null;
            contextMap.put("medicines", medicines != null ? medicines : List.of());
            log.info("获取药品列表成功，数量: {}", medicines != null ? medicines.size() : 0);
        } catch (Exception e) {
            log.error("获取药品列表失败，elderId: {}", elderId, e);
            contextMap.put("medicines", List.of());
        }

        try {
            Result<List<Map<String, Object>>> result = medicineFeignClient.getTodayRecords(elderId);
            List<Map<String, Object>> todayRecords = result != null ? result.getData() : null;
            contextMap.put("todayRecords", todayRecords != null ? todayRecords : List.of());
            log.info("获取今日用药记录成功，数量: {}", todayRecords != null ? todayRecords.size() : 0);
        } catch (Exception e) {
            log.error("获取今日用药记录失败，elderId: {}", elderId, e);
            contextMap.put("todayRecords", List.of());
        }

        try {
            Result<List<Map<String, Object>>> result = remindFeignClient.getTodayTasks();
            List<Map<String, Object>> todayTasks = result != null ? result.getData() : null;
            contextMap.put("todayTasks", todayTasks != null ? todayTasks : List.of());
            log.info("获取今日提醒任务成功，数量: {}", todayTasks != null ? todayTasks.size() : 0);
        } catch (Exception e) {
            log.error("获取今日提醒任务失败，elderId: {}", elderId, e);
            contextMap.put("todayTasks", List.of());
        }

        return JSONUtil.toJsonStr(contextMap);
    }

    /**
     * 生成模拟AI回答
     * 当前为模拟实现，后续将接入DashScope进行真正的AI问答
     *
     * @param question 用户问题
     * @param context  上下文数据
     * @return 模拟回答
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
     * 创建SSE事件
     *
     * @param event 事件类型
     * @param data  事件数据
     * @return ServerSentEvent
     */
    private ServerSentEvent<String> createSseEvent(String event, String data) {
        return ServerSentEvent.<String>builder()
                .event(event)
                .data(data)
                .build();
    }
}

package com.elderlycare.ai.orchestration;

import com.elderlycare.common.Result;
import com.elderlycare.common.exception.BusinessException;
import com.elderlycare.pojo.vo.RemindTaskVO;
import com.elderlycare.service.AIAssistantService;
import com.elderlycare.ai.provider.MedicineAIProvider;
import com.elderlycare.ai.context.MedicineContextBuilder;
import com.elderlycare.ai.notification.MedicineNotifier;
import com.elderlycare.service.RemindService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.elderlycare.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIAssistantServiceImpl implements AIAssistantService {

    private final MedicineContextBuilder contextBuilder;
    private final MedicineAIProvider aiProvider;
    private final MedicineNotifier notifier;
    private final RemindService remindService;

    @Override
    public Flux<ServerSentEvent<String>> processMedicineQuestionStream(Integer elderId, String userInput) {
        if (elderId == null || !StringUtils.hasText(userInput)) {
            log.warn("用药问题咨询参数无效: elderId={}, userInput={}", elderId, userInput);
            throw new BusinessException(PARAM_ERROR.getCode(), "请求参数不能为空");
        }

        log.info("处理老人 ID: {} 的用药问题咨询（流式）", elderId);

        String context = contextBuilder.buildContextForQuestion(elderId);

        return aiProvider.getResponseForQuestionStream(context, userInput)
                .map(content -> ServerSentEvent.<String>builder()
                        .data(content)
                        .event("message")
                        .id(String.valueOf(System.currentTimeMillis()))
                        .build())
                .onErrorResume(error -> {
                    return Flux.just(ServerSentEvent.<String>builder()
                            .data("抱歉，AI服务暂时不可用，请稍后再试。")
                            .event("error")
                            .id(String.valueOf(System.currentTimeMillis()))
                            .build());
                });
    }

    @Override
    public void initiateMedicineReminder(Integer elderId) {
        if (elderId == null) {
            log.warn("发起用药提醒参数无效: elderId={}", elderId);
            throw new BusinessException(PARAM_ERROR.getCode(), "老人ID不能为空");
        }

        log.info("发起老人 ID: {} 的用药提醒", elderId);
        String context = contextBuilder.buildContextForReminder(elderId);
        String aiReminder = aiProvider.getReminderMessage(context);
        notifier.sendMedicineReminder(elderId, aiReminder);
        log.info("成功发送老人 ID: {} 的用药提醒", elderId);
    }

    @Override
    public void handleMissedMedicine(Integer elderId, Integer taskId) {
        if (elderId == null || taskId == null) {
            log.warn("处理漏服干预参数无效: elderId={}, taskId={}", elderId, taskId);
            throw new BusinessException(PARAM_ERROR.getCode(), "老人ID和任务ID不能为空");
        }

        log.info("处理老人 ID: {} 的任务 ID: {} 的漏服干预", elderId, taskId);
        String context = contextBuilder.buildContextForMissed(elderId, taskId);
        String aiIntervention = aiProvider.getMissedInterventionMessage(context);
        notifier.sendMissedMedicineIntervention(elderId, aiIntervention);
        log.info("成功发送老人 ID: {} 的漏服干预消息", elderId);
    }

    @Override
    public void checkAndRemindTodayMedicines() {
        log.info("开始执行每日用药提醒检查...");

        List<RemindTaskVO> todayTasks = remindService.getAllTodayTasks().getData();

        if (todayTasks == null || todayTasks.isEmpty()) {
            log.info("今日暂无提醒任务");
            return;
        }

        List<RemindTaskVO> medicineTasks = todayTasks.stream()
                .filter(task -> task.getRemindType() != null && task.getRemindType() == 1)
                .filter(task -> task.getStatus() != null && task.getStatus() == 1)
                .toList();

        if (medicineTasks.isEmpty()) {
            log.info("今日暂无用药提醒任务");
            return;
        }

        Map<Integer, List<RemindTaskVO>> tasksByElder = medicineTasks.stream()
                .filter(task -> task.getElderId() != null)
                .collect(Collectors.groupingBy(RemindTaskVO::getElderId));

        log.info("发现 {} 个老人有用药提醒任务", tasksByElder.size());

        int successCount = 0;
        int failCount = 0;

        for (Map.Entry<Integer, List<RemindTaskVO>> entry : tasksByElder.entrySet()) {
            Integer elderId = entry.getKey();
            List<RemindTaskVO> tasks = entry.getValue();

            try {
                log.info("为老人 ID: {} 生成用药提醒，共 {} 个任务", elderId, tasks.size());
                initiateMedicineReminder(elderId);
                successCount++;
            } catch (Exception e) {
                log.error("为老人 ID: {} 生成用药提醒失败", elderId, e);
                failCount++;
            }
        }

        log.info("每日用药提醒检查完成，成功: {}, 失败: {}", successCount, failCount);
    }
}

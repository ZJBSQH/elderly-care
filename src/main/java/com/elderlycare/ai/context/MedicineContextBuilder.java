package com.elderlycare.ai.context;

import com.elderlycare.pojo.vo.MedicineVO;
import com.elderlycare.pojo.vo.RecordVO;
import com.elderlycare.pojo.vo.RemindTaskVO;
import com.elderlycare.service.MedicineService;
import com.elderlycare.service.RecordService;
import com.elderlycare.service.RemindService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MedicineContextBuilder {

    private final MedicineService medicineService;
    private final RecordService recordService;
    private final RemindService remindService;

    /**
     * 构建用药问答上下文（详细版）
     */
    public String buildContextForQuestion(Integer elderId) {
        validateElderId(elderId);

        ContextData data = loadContextData(elderId);

        if (data.isEmpty()) {
            return "该老人暂无用药计划和提醒任务。";
        }

        return buildContext(elderId, data, ContextFormat.DETAILED);
    }

    /**
     * 构建用药提醒上下文（简洁版）
     */
    public String buildContextForReminder(Integer elderId) {
        validateElderId(elderId);

        ContextData data = loadContextData(elderId);

        if (data.isEmpty()) {
            return "该老人暂无用药计划和提醒任务。";
        }

        return buildContext(elderId, data, ContextFormat.SIMPLE);
    }

    /**
     * 构建漏服干预上下文
     */
    public String buildContextForMissed(Integer elderId, Integer taskId) {
        RemindTaskVO task = remindService.getTaskById(taskId).getData();
        List<MedicineVO> medicines = safeGetList(medicineService.selectByElderId(elderId).getData());

        String medicineInfo = medicines.stream()
                .filter(m -> m.getId().equals(task.getMedicineId()))
                .findFirst()
                .map(this::formatMedicineInfo)
                .orElse("未知药品");

        return String.format(
                "老人 ID: %d, 今日漏服的计划任务 ID: %d, 任务详情: %s, 对应药品信息: %s。",
                elderId, taskId, task.getTitle(), medicineInfo
        );
    }

    // ==================== 私有方法 ====================

    /**
     * 统一的上下文构建逻辑
     */
    private String buildContext(Integer elderId, ContextData data, ContextFormat format) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("老人 ID: ").append(elderId).append("\n");

        appendSection(sb, "--- 用药计划 ---\n", data.medicines(),
                format::formatMedicine, "暂无用药计划");
        appendSection(sb, "--- 今日待办提醒 ---\n", data.tasks(),
                format::formatTask, "暂无提醒任务");
        appendSection(sb, "--- 今日服药记录 ---\n", data.records(),
                format::formatRecord, "暂无服药记录");

        return sb.toString();
    }

    /**
     * 通用段落拼接方法
     */
    private <T> void appendSection(StringBuilder sb, String header,
                                   List<T> items, Formatter<T> formatter, String emptyText) {
        sb.append(header);
        if (items.isEmpty()) {
            sb.append(emptyText).append("\n");
            return;
        }
        items.forEach(item -> sb.append(formatter.format(item)));
    }

    /**
     * 统一加载所有需要的数据
     */
    private ContextData loadContextData(Integer elderId) {
        return new ContextData(
                safeGetList(medicineService.selectByElderId(elderId).getData()),
                safeGetList(remindService.getTodayTasks().getData()),
                safeGetList(recordService.getTodayRecords(elderId).getData())
        );
    }

    /**
     * 安全获取列表（空值保护）
     */
    private <T> List<T> safeGetList(List<T> list) {
        return Optional.ofNullable(list).orElse(Collections.emptyList());
    }

    /**
     * 验证老人ID
     */
    private void validateElderId(Integer elderId) {
        if (elderId == null) {
            throw new IllegalArgumentException("老人ID不能为空");
        }
    }

    /**
     * 格式化药品信息
     */
    private String formatMedicineInfo(MedicineVO medicine) {
        return String.format("药品名称: %s, 用法用量: %s, 服药频率: %s",
                medicine.getMedicineName(), medicine.getDosage(), medicine.getFrequency());
    }

    // ==================== 内部类和枚举 ====================

    /**
     * 上下文数据封装
     */
    private record ContextData(
            List<MedicineVO> medicines,
            List<RemindTaskVO> tasks,
            List<RecordVO> records
    ) {
        boolean isEmpty() {
            return medicines.isEmpty() && tasks.isEmpty();
        }
    }

    /**
     * 上下文格式枚举
     */
    private enum ContextFormat {
        DETAILED {
            @Override
            String formatMedicine(MedicineVO m) {
                return String.format("ID: %d, 名称: %s, 用量: %s, 服药时间: %s, 频率: %s\n",
                        m.getId(), m.getMedicineName(), m.getDosage(),
                        m.getRemindTime(), m.getFrequency());
            }

            @Override
            String formatTask(RemindTaskVO t) {
                return String.format("ID: %d, 标题: %s, 时间: %s, 类型: %s\n",
                        t.getId(), t.getTitle(), t.getRemindTime(), t.getRemindType());
            }

            @Override
            String formatRecord(RecordVO r) {
                return String.format("任务ID: %d, 状态: %s, 时间: %s\n",
                        r.getTaskId(), getRecordStatusText(r.getStatus()), r.getRecordTime());
            }
        },
        SIMPLE {
            @Override
            String formatMedicine(MedicineVO m) {
                return String.format("名称: %s, 用量: %s, 服药时间: %s\n",
                        m.getMedicineName(), m.getDosage(), m.getRemindTime());
            }

            @Override
            String formatTask(RemindTaskVO t) {
                return String.format("标题: %s, 时间: %s\n", t.getTitle(), t.getRemindTime());
            }

            @Override
            String formatRecord(RecordVO r) {
                return r.getStatus() == 1 ?
                        String.format("任务ID: %d, 已服\n", r.getTaskId()) : "";
            }
        };

        abstract String formatMedicine(MedicineVO m);
        abstract String formatTask(RemindTaskVO t);
        abstract String formatRecord(RecordVO r);
    }

    /**
     * 格式化器函数式接口
     */
    @FunctionalInterface
    private interface Formatter<T> {
        String format(T item);
    }

    /**
     * 状态文本转换
     */
    private static String getRecordStatusText(Integer status) {
        if (status == null) return "未知状态";
        return switch (status) {
            case 0 -> "未服";
            case 1 -> "已服";
            case 2 -> "漏服";
            case 3 -> "跳过";
            default -> "未知状态";
        };
    }
}

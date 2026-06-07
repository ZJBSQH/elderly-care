package com.elderlycare.ai.service.impl;

import com.elderlycare.ai.feign.MedicineFeignClient;
import com.elderlycare.ai.feign.RemindFeignClient;
import com.elderlycare.ai.service.MedicalContextBuilder;
import com.elderlycare.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 *上下文实现
 * 使用feign调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalContextBuilderImpl implements MedicalContextBuilder {

    /** 药品服务 Feign 客户端，获取药品清单和用药记录 */
    private final MedicineFeignClient medicineFeignClient;

    /** 提醒服务 Feign 客户端，获取今日提醒任务 */
    private final RemindFeignClient remindFeignClient;


    @Override
    public String buildAndFormat(Integer elderId) {

        log.info("构建上下文({})", elderId);

        //使用LinkedHashMap保存数据
        Map<String,Object> contextMap = new LinkedHashMap<>();

        //数据:药品清单
        try{
            Result<List<Map<String, Object>>> result = medicineFeignClient.getMedicinesByElderId(elderId);
            List<Map<String, Object>> medicines =
                    result != null ? result.getData() : null;
            contextMap.put("medicines", medicines);
            log.info("获取成功,数量:{}",medicines != null ? medicines.size() : 0);
        }catch (Exception e){
            log.error("获取药品列表失败，elderId: {}", elderId, e);
            contextMap.put("medicines", List.of());
        }

        // 数据源2：今日用药记录
        try {
            Result<List<Map<String, Object>>> result =
                    medicineFeignClient.getTodayRecords(elderId);
            List<Map<String, Object>> records =
                    result != null ? result.getData() : null;
            contextMap.put("todayRecords", records != null ? records : List.of());
            log.info("获取今日用药记录成功，数量: {}",
                    records != null ? records.size() : 0);
        } catch (Exception e) {
            log.error("获取今日用药记录失败，elderId: {}", elderId, e);
            contextMap.put("todayRecords", List.of());
        }

        // 数据源3：今日提醒任务
        try {
            Result<List<Map<String, Object>>> result =
                    remindFeignClient.getTodayTasks();
            List<Map<String, Object>> tasks =
                    result != null ? result.getData() : null;
            contextMap.put("todayTasks", tasks != null ? tasks : List.of());
            log.info("获取今日提醒任务成功，数量: {}",
                    tasks != null ? tasks.size() : 0);
        } catch (Exception e) {
            log.error("获取今日提醒任务失败，elderId: {}", elderId, e);
            contextMap.put("todayTasks", List.of());
        }

        // 核心改进：格式化为自然语言文本而非 JSON
        return formatToNaturalLanguage(contextMap);
    }

    /**
     * 将结构上下文转为自然语言文本
     */
    @SuppressWarnings("unchecked")
    public String formatToNaturalLanguage(Map<String, Object> contextMap){
        StringBuilder sb = new StringBuilder();

        //药品清单
        List<Map<String, Object>> medicines = (List<Map<String, Object>>) contextMap.get("medicines");
        sb.append("药品清单\n");

        if(medicines.isEmpty()){
            sb.append("(暂无药品数据)\n");
        }else {
            int index = 1;
            for (Map<String, Object> m : medicines) {
                sb.append(String.format("%d,%s,%s,%s,提醒时间 %s\n",
                        index++,
                        m.getOrDefault("medicineName", "未知"),
                        m.getOrDefault("dosage", ""),
                        m.getOrDefault("frequency", ""),
                        m.getOrDefault("remindTime", "")));
            }
        }
        // 今日用药记录
        List<Map<String, Object>> records =
                (List<Map<String, Object>>) contextMap.getOrDefault("todayRecords", List.of());
        sb.append("\n【今日用药记录】\n");
        if (records.isEmpty()) {
            sb.append("（暂无今日用药记录）\n");
        } else {
            for (int i = 0; i < records.size(); i++) {
                Map<String, Object> r = records.get(i);
                sb.append(String.format("%d. %s：%s（%s）\n",
                        i + 1,
                        r.getOrDefault("medicineName", "未知"),
                        r.getOrDefault("status", "未知"),
                        r.getOrDefault("actualTime", "未知")));
            }
        }
        // 今日提醒任务
        List<Map<String, Object>> tasks =
                (List<Map<String, Object>>) contextMap.getOrDefault("todayTasks", List.of());
        sb.append("\n【今日提醒】\n");
        if (tasks.isEmpty()) {
            sb.append("（暂无提醒任务）\n");
        } else {
            for (int i = 0; i < tasks.size(); i++) {
                Map<String, Object> t = tasks.get(i);
                sb.append(String.format("%d. %s %s\n",
                        i + 1,
                        t.getOrDefault("remindTime", ""),
                        t.getOrDefault("title", "")));
            }
        }

        return sb.toString();
    }

}

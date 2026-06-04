package com.elderlycare.ai.feign;

import com.elderlycare.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 药品服务 Feign 客户端
 * <p>
 * 通过声明式 HTTP 调用 elderly-medicine 服务，
 * 获取老人的药品清单和用药记录，为 AI 问答提供上下文数据。
 *
 * @author 郑
 */
@FeignClient(name = "elderly-medicine")
public interface MedicineFeignClient {

    /**
     * 根据老人ID查询药品清单
     *
     * @param elderId 老人ID
     * @return 药品列表
     */
    @GetMapping("/medicine/look/{elderId}")
    Result<List<Map<String, Object>>> getMedicinesByElderId(@PathVariable("elderId") Integer elderId);

    /**
     * 查询老人今日用药记录
     *
     * @param elderId 老人ID
     * @return 今日用药记录列表（含服药状态、时间等）
     */
    @GetMapping("/record/today")
    Result<List<Map<String, Object>>> getTodayRecords(@RequestParam("elderId") Integer elderId);
}

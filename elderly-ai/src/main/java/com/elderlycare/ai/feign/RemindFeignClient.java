package com.elderlycare.ai.feign;

import com.elderlycare.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * 提醒服务 Feign 客户端
 * <p>
 * 通过声明式 HTTP 调用 elderly-remind 服务，
 * 获取今日用药提醒任务，为 AI 问答提供提醒上下文。
 *
 * @author 郑
 */
@FeignClient(name = "elderly-remind", configuration = com.elderlycare.ai.config.FeignConfig.class)
public interface RemindFeignClient {

    /**
     * 获取全平台今日提醒任务列表
     * <p>
     * 返回所有用户的今日提醒任务，包含用药提醒、健康检查提醒等。
     *
     * @return 今日提醒任务列表
     */
    @GetMapping("/remind/task/today")
    Result<List<Map<String, Object>>> getTodayTasks();
}

package com.elderlycare.ai.feign;

import com.elderlycare.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * 提醒服务Feign客户端
 *
 * @author 郑
 */
@FeignClient(name = "elderly-remind")
public interface RemindFeignClient {

    @GetMapping("/remind/task/today")
    Result<List<Map<String, Object>>> getTodayTasks();
}

package com.elderlycare.ai.feign;

import com.elderlycare.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 药品服务Feign客户端
 *
 * @author 郑
 */
@FeignClient(name = "elderly-medicine")
public interface MedicineFeignClient {

    @GetMapping("/medicine/look/{elderId}")
    Result<List<Map<String, Object>>> getMedicinesByElderId(@PathVariable("elderId") Integer elderId);

    @GetMapping("/record/today")
    Result<List<Map<String, Object>>> getTodayRecords(@RequestParam("elderId") Integer elderId);
}

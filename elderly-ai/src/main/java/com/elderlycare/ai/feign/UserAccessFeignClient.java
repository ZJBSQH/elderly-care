package com.elderlycare.ai.feign;

import com.elderlycare.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 用户访问权限 Feign 客户端
 * 用于将当前登录老人用户解析为老人档案 ID。
 */
@FeignClient(name = "elderly-user", configuration = com.elderlycare.ai.config.FeignConfig.class)
public interface UserAccessFeignClient {

    /**
     * 获取当前老人用户对应的老人档案 ID。
     */
    @GetMapping("/user/access/current-elder-id")
    Result<Integer> getCurrentElderId();
}

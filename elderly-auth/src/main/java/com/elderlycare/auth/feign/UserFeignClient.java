package com.elderlycare.auth.feign;

import com.elderlycare.auth.entity.Elder;
import com.elderlycare.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 调用user服务的Feign客户端，用于老人档案的查询与创建
 */
@FeignClient(name = "elderly-user",path = "/user/elder")
public interface UserFeignClient {

    /**
     * 根据用户ID查询老人档案
     */
    @GetMapping("/byUserId")
    Result<Elder> getElderByUserId(@RequestParam("userId") Integer userId);

    /**
     * 根据用户ID创建老人档案
     */
    @PostMapping("/create")
    public Result<Elder> create(@RequestParam("userId") Integer userId);
}
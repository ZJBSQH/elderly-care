package com.elderlycare.medicine.feign;

import com.elderlycare.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户访问权限 Feign 客户端
 * 调用 user 服务判断当前用户是否可以访问指定老人档案。
 */
@FeignClient(name = "elderly-user")
public interface UserAccessFeignClient {

    /**
     * 判断当前用户是否可以访问指定老人档案。
     */
    @GetMapping("/user/access/elder")
    Result<Boolean> canAccessElder(@RequestParam("elderId") Integer elderId);
}

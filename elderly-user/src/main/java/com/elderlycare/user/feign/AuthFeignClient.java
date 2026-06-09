package com.elderlycare.user.feign;

import com.elderlycare.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 认证服务Feign客户端
 */
@FeignClient(name = "elderly-auth")
public interface AuthFeignClient {

    /**
     * 根据手机号查询用户信息
     */
    @GetMapping("/auth/user/byPhone")
    Result<Map<String, Object>> getUserByPhone(@RequestParam("phone") String phone);

    /**
     * 根据用户ID查询用户信息
     */
    @GetMapping("/auth/user/byId")
    Result<Map<String, Object>> getUserById(@RequestParam("id") Integer id);
}

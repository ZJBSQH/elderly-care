package com.elderlycare.user.feign;

import com.elderlycare.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "elderly-auth")
public interface AuthFeignClient {

    @GetMapping("/auth/user/byPhone")
    Result<Map<String, Object>> getUserByPhone(@RequestParam("phone") String phone);

    @GetMapping("/auth/user/byId")
    Result<Map<String, Object>> getUserById(@RequestParam("id") Integer id);
}

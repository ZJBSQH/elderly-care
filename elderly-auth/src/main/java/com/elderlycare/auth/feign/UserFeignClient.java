package com.elderlycare.auth.feign;

import com.elderlycare.auth.entity.Elder;
import com.elderlycare.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "elderly-user",path = "/user/elder")
public interface UserFeignClient {

    @GetMapping("/byUserId")
    Result<Elder> getElderByUserId(@RequestParam("userId") Integer userId);

    @PostMapping("/create")
    public Result<Elder> create(@RequestParam("userId") Integer userId);
}
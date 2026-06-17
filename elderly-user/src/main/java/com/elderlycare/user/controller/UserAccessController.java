package com.elderlycare.user.controller;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户数据访问权限控制器
 * 提供给其他微服务校验当前用户是否可以访问指定老人档案。
 */
@RestController
@RequestMapping("/user/access")
@RequiredArgsConstructor
public class UserAccessController {

    private final UserService userService;

    /**
     * 判断当前登录用户是否可以访问指定老人档案。
     */
    @GetMapping("/elder")
    public Result<Boolean> canAccessElder(@RequestParam Integer elderId) {
        return userService.canAccessElder(elderId);
    }

    /**
     * 获取当前老人用户对应的老人档案 ID。
     */
    @GetMapping("/current-elder-id")
    public Result<Integer> getCurrentElderId() {
        return userService.getCurrentElderId();
    }
}

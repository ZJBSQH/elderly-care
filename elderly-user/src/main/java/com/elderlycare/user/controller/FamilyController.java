package com.elderlycare.user.controller;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.user.dto.FamilyBindConfirmRequest;
import com.elderlycare.user.dto.FamilyBindRequest;
import com.elderlycare.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 家属绑定控制器
 */
@RestController
@RequestMapping("/user/family")
@RequiredArgsConstructor
public class FamilyController {

    private final UserService userService;

    /**
     * 家属通过手机号绑定老人
     */
    @PostMapping("/bind")
    public Result<Void> bindFamily(@Valid @RequestBody FamilyBindRequest request) {
        return userService.bindFamily(request);
    }

    /**
     * 家属通过扫描二维码确认绑定
     */
    @PostMapping("/bind/confirm")
    public Result<Void> bindElderByQRCode(@Valid @RequestBody FamilyBindConfirmRequest request) {
        return userService.bindElderByQRCode(request);
    }

    /**
     * 家属解绑老人
     */
    @DeleteMapping("/unbind")
    public Result<Void> unbindFamily(@RequestParam Integer elderId) {
        return userService.unbindFamily(elderId);
    }

    /**
     * 获取当前家属已绑定的老人列表
     */
    @GetMapping("/elders")
    public Result<?> getBoundElders() {
        return userService.getBoundElders();
    }
}

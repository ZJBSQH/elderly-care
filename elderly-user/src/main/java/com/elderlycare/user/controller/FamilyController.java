package com.elderlycare.user.controller;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.user.dto.FamilyBindConfirmRequest;
import com.elderlycare.user.dto.FamilyBindRequest;
import com.elderlycare.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/family")
@RequiredArgsConstructor
public class FamilyController {

    private final UserService userService;

    @PostMapping("/bind")
    public Result<Void> bindFamily(@Valid @RequestBody FamilyBindRequest request) {
        return userService.bindFamily(request);
    }

    @PostMapping("/bind/confirm")
    public Result<Void> bindElderByQRCode(@Valid @RequestBody FamilyBindConfirmRequest request) {
        return userService.bindElderByQRCode(request);
    }

    @DeleteMapping("/unbind")
    public Result<Void> unbindFamily(@RequestParam Integer elderId) {
        return userService.unbindFamily(elderId);
    }

    @GetMapping("/elders")
    public Result<?> getBoundElders() {
        return userService.getBoundElders();
    }
}

package com.elderlycare.auth.controller;

import com.elderlycare.auth.dto.*;
import com.elderlycare.auth.service.AuthService;
import com.elderlycare.auth.vo.UserVO;
import com.elderlycare.common.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sms")
    public Result<Void> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        return authService.sendSmsCode(request);
    }

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return authService.updateProfile(request);
    }

    @GetMapping("/profile")
    public Result<Map<String, Object>> getUserInfo() {
        return authService.getCurrentUserInfo();
    }

    @PostMapping("/password/reset")
    public Result<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        return authService.resetPassword(request);
    }

    @PutMapping("/password/change")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        return authService.changePassword(request);
    }

    @GetMapping("/user/byPhone")
    public Result<Map<String, Object>> getUserByPhone(@RequestParam String phone) {
        return authService.getUserByPhone(phone);
    }

    @GetMapping("/user/byId")
    public Result<Map<String, Object>> getUserById(@RequestParam Integer id) {
        return authService.getUserById(id);
    }
}

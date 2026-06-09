package com.elderlycare.auth.controller;

import com.elderlycare.auth.dto.*;
import com.elderlycare.auth.service.AuthService;
import com.elderlycare.auth.vo.UserVO;
import com.elderlycare.common.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器，处理用户注册、登录、短信验证码、个人信息管理、密码修改等请求
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms")
    public Result<Void> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        return authService.sendSmsCode(request);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * 更新当前用户个人信息
     */
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return authService.updateProfile(request);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getUserInfo() {
        return authService.getCurrentUserInfo();
    }

    /**
     * 通过短信验证码重置密码
     */
    @PostMapping("/password/reset")
    public Result<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        return authService.resetPassword(request);
    }

    /**
     * 修改密码（需要原密码验证）
     */
    @PutMapping("/password/change")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        return authService.changePassword(request);
    }

    /**
     * 根据手机号查询用户信息
     */
    @GetMapping("/user/byPhone")
    public Result<Map<String, Object>> getUserByPhone(@RequestParam String phone) {
        return authService.getUserByPhone(phone);
    }

    /**
     * 根据用户ID查询用户信息
     */
    @GetMapping("/user/byId")
    public Result<Map<String, Object>> getUserById(@RequestParam Integer id) {
        return authService.getUserById(id);
    }
}

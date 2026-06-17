package com.elderlycare.controller.user;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.dto.*;
import com.elderlycare.pojo.dto.auth.*;
import com.elderlycare.pojo.vo.UserVO;
import com.elderlycare.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;  // ✅ 添加 private final

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms")
    public Result<Void> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        return userService.sendSmsCode(request);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    /**
     * 用户信息更改
     */
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return userService.updateProfile(request);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getUserInfo() {
        return userService.getCurrentUserInfo();
    }

    /**
     * 重叠密码
     */
    @PostMapping("/password/reset")
    public Result<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        return userService.resetPassword(request);
    }

    @PutMapping("/password/change")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        return userService.changePassword(request);
    }
    /**
     * 绑定家人
     */
    @PostMapping("/family/bind")
    public Result<Void> bindFamily(@Valid @RequestBody FamilyBindRequest request) {
        return userService.bindFamily(request);
    }

    /**
     * 老人生成专属二维码
     */
    @GetMapping("/family/code/generate")
    public Result<String> generateElderQRCode() {
        return userService.generateElderQRCode();
    }

    /**
     * 家属扫描二维码获取老人信息
     */
    @GetMapping("/code/parse")
    public Result<ElderInfoDTO> getElderByQRCodeToken(@RequestParam String qrCodeToken) {
        if (qrCodeToken == null || qrCodeToken.isEmpty()) {
            return Result.error("二维码 token 不能为空");
        }
        return userService.parseQRCode(qrCodeToken);
    }

    /**
     * 家属确认绑定老人
     */
    @PostMapping("/bind/confirm")
    public Result<Void> bindElderConfirm(@Valid @RequestBody FamilyBindConfirmRequest request) {
        return userService.bindElderSimple(request);
    }
}


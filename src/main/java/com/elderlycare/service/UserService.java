package com.elderlycare.service;

import com.elderlycare.pojo.dto.*;
import com.elderlycare.pojo.dto.auth.*;
import com.elderlycare.pojo.entity.User;
import com.elderlycare.pojo.vo.UserVO;
import com.elderlycare.common.Result;

import java.util.Map;

public interface UserService {
    // 发送验证码
    Result<Void> sendSmsCode(SmsCodeRequest request);
    // 注册
    Result<UserVO> register(RegisterRequest request);
    //登录
    Result<Map<String, Object>> login(LoginRequest request);
    // 根据手机号查询用户
    User findByPhone(String phone);
    // 保存用户
    void saveUser(User user);
    // 更新用户信息
    Result<UserVO> updateProfile(ProfileUpdateRequest request);
    // 忘记密码
    Result<Void> resetPassword(PasswordResetRequest request);
    // 修改密码
    Result<Void> changePassword(PasswordChangeRequest request);
    // 绑定家人
    Result<Void> bindFamily(FamilyBindRequest  request);
    /**
     * 老人生成专属二维码
     */
    Result<String> generateElderQRCode();

    /**
     * 家属扫描二维码获取老人信息
     */
    Result<ElderInfoDTO> parseQRCode(String qrCodeToken);

    /**
     * 家属绑定老人（简化版）
     */
    Result<Void> bindElderSimple(FamilyBindConfirmRequest request);
    
    /**
     * 获取当前登录用户信息
     */
    Result<Map<String, Object>> getCurrentUserInfo();
}


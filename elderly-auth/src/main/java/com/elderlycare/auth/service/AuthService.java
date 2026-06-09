package com.elderlycare.auth.service;

import com.elderlycare.auth.dto.*;
import com.elderlycare.auth.entity.User;
import com.elderlycare.auth.vo.UserVO;
import com.elderlycare.common.core.result.Result;

import java.util.Map;

/**
 * 认证服务接口
 */
public interface AuthService {
    /**
     * 发送短信验证码
     */
    Result<Void> sendSmsCode(SmsCodeRequest request);
    /**
     * 用户注册
     */
    Result<UserVO> register(RegisterRequest request);
    /**
     * 用户登录
     */
    Result<Map<String, Object>> login(LoginRequest request);
    /**
     * 根据手机号查询用户
     */
    Result<User> findByPhone(String phone);
    /**
     * 保存用户信息
     */
    Result<Void> saveUser(User user);
    /**
     * 更新用户个人信息
     */
    Result<UserVO> updateProfile(ProfileUpdateRequest request);
    /**
     * 通过短信验证码重置密码
     */
    Result<Void> resetPassword(PasswordResetRequest request);
    /**
     * 修改密码（需原密码验证）
     */
    Result<Void> changePassword(PasswordChangeRequest request);
    /**
     * 获取当前登录用户信息
     */
    Result<Map<String, Object>> getCurrentUserInfo();
    /**
     * 根据手机号查询用户信息（对外Feign调用）
     */
    Result<Map<String, Object>> getUserByPhone(String phone);
    /**
     * 根据用户ID查询用户信息（对外Feign调用）
     */
    Result<Map<String, Object>> getUserById(Integer id);
}

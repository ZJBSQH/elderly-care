package com.elderlycare.auth.service;

import com.elderlycare.auth.dto.*;
import com.elderlycare.auth.entity.User;
import com.elderlycare.common.vo.UserVO;
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
     * 根据用户标识查询用户信息（对外Feign调用，不含密码）
     * 支持通过手机号或用户ID查询
     *
     * @param identifier 用户标识（手机号或用户ID）
     * @param isPhone 是否为手机号查询（true: 手机号，false: 用户ID）
     * @return 用户信息
     */
    Result<UserVO> getUserByIdentifier(String identifier, boolean isPhone);
}

package com.elderlycare.auth.service;

import com.elderlycare.auth.dto.*;
import com.elderlycare.auth.entity.User;
import com.elderlycare.auth.vo.UserVO;
import com.elderlycare.common.core.result.Result;

import java.util.Map;

public interface AuthService {
    Result<Void> sendSmsCode(SmsCodeRequest request);
    Result<UserVO> register(RegisterRequest request);
    Result<Map<String, Object>> login(LoginRequest request);
    Result<User> findByPhone(String phone);
    Result<Void> saveUser(User user);
    Result<UserVO> updateProfile(ProfileUpdateRequest request);
    Result<Void> resetPassword(PasswordResetRequest request);
    Result<Void> changePassword(PasswordChangeRequest request);
    Result<Map<String, Object>> getCurrentUserInfo();
    Result<Map<String, Object>> getUserByPhone(String phone);
    Result<Map<String, Object>> getUserById(Integer id);
}

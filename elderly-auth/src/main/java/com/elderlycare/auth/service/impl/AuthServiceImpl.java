package com.elderlycare.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.elderlycare.auth.dto.*;
import com.elderlycare.auth.entity.Elder;
import com.elderlycare.auth.entity.User;
import com.elderlycare.auth.feign.UserFeignClient;
import com.elderlycare.auth.mapper.UserMapper;
import com.elderlycare.auth.service.AuthService;
import com.elderlycare.auth.vo.UserVO;
import com.elderlycare.common.core.exception.BusinessException;
import com.elderlycare.common.core.result.Result;
import com.elderlycare.common.security.util.JwtUtil;
import com.elderlycare.common.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.elderlycare.auth.dto.AuthErrorCode.*;
import static com.elderlycare.common.core.exception.BaseErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final Duration SMS_CODE_EXPIRE = Duration.ofMinutes(5);
    private static final Integer USER_TYPE_ELDER = 0;
    private static final Integer USER_TYPE_FAMILY = 1;
    private static final Integer USER_STATUS_ENABLE = 1;

    private final UserMapper userMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SecurityUtil securityUtil;
    private final UserFeignClient userFeignClient;

    @Override
    public Result<Void> sendSmsCode(SmsCodeRequest request) {
        String phone = request.getPhone();
        String type = request.getType();

        User existUser = userMapper.selectByPhone(phone);
        switch (type) {
            case "register" -> { if (existUser != null) throw new BusinessException(PHONE_ALREADY_REGISTERED); }
            case "login", "reset" -> { if (existUser == null) throw new BusinessException(USER_NOT_EXIST); }
            default -> throw new BusinessException(PARAM_ERROR, "验证码类型仅支持register/login/reset");
        }

        String smsCode = RandomUtil.randomNumbers(6);
        redisTemplate.opsForValue().set(SMS_CODE_PREFIX + phone, smsCode, SMS_CODE_EXPIRE);
        log.info("为手机号{}生成{}类型验证码：{}", phone, type, smsCode);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<UserVO> register(RegisterRequest request) {
        String phone = request.getPhone();
        validateSmsCode(phone, request.getSmsCode());
        if (userMapper.selectByPhone(phone) != null) {
            throw new BusinessException(PHONE_ALREADY_REGISTERED);
        }

        Integer userType = request.getUserType() != null ? request.getUserType()
                : (request.getAge() != null && request.getAge() >= 60) ? USER_TYPE_ELDER : USER_TYPE_FAMILY;

        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setSex(request.getSex());
        user.setAge(request.getAge());
        user.setUserType(userType);
        user.setStatus(USER_STATUS_ENABLE);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        log.info("用户{}注册成功，ID：{}", phone, user.getId());

        // 老人用户注册时同步创建 Elder 档案
        if (user.getUserType() == 0) {
            try {
                Result<Elder> elderResult = userFeignClient.create(user.getId());
                log.info("为老人用户{}创建Elder档案，elderId: {}", user.getId(),
                        elderResult != null && elderResult.getData() != null ? elderResult.getData().getId() : "创建失败");
            } catch (Exception e) {
                log.error("创建Elder档案失败，userId: {}, 错误: {}", user.getId(), e.getMessage());
                throw new BusinessException("老人档案创建失败，请稍后重试");
            }
        }

        redisTemplate.delete(SMS_CODE_PREFIX + phone);
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return Result.success(vo);
    }

    @Override
    public Result<Map<String, Object>> login(LoginRequest request) {
        User user = userMapper.selectByPhone(request.getPhone());
        if (user == null) throw new BusinessException(PHONE_NOT_REGISTERED);
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(PASSWORD_ERROR);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getUserType());
        log.info("用户{}登录成功", request.getPhone());

        Map<String, Object> result = new HashMap<>();
        user.setPassword(null);
        result.put("user", user);
        result.put("token", token);
        
        // 老人用户：查询或自动创建 Elder 档案，确保 elderId 可用
        if (user.getUserType() == 0) {
            Integer elderId = getOrCreateElder(user.getId());
            if (elderId != null) {
                result.put("elderId", elderId);
            }
        }
        
        return Result.success(result);
    }

    @Override
    public Result<User> findByPhone(String phone) {
        return Result.success(userMapper.selectByPhone(phone));
    }

    @Override
    public Result<Void> saveUser(User user) {
        userMapper.insert(user);
        return Result.success();
    }

    @Override
    public Result<UserVO> updateProfile(ProfileUpdateRequest request) {
        User user = userMapper.selectById(request.getId());
        if (user == null) throw new BusinessException(USER_NOT_EXIST);

        if (request.getName() != null) user.setName(request.getName());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getAge() != null) user.setAge(request.getAge());
        if (request.getSex() != null) user.setSex(request.getSex());
        userMapper.updateById(user);

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return Result.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> resetPassword(PasswordResetRequest request) {
        String phone = request.getPhone();
        User user = userMapper.selectByPhone(phone);
        if (user == null) throw new BusinessException(PHONE_NOT_REGISTERED);

        validateSmsCode(phone, request.getSmsCode());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        redisTemplate.delete(SMS_CODE_PREFIX + phone);
        return Result.success();
    }

    @Override
    public Result<Void> changePassword(PasswordChangeRequest request) {
        User user = userMapper.selectByPhone(request.getPhone());
        if (user == null) throw new BusinessException(PHONE_NOT_REGISTERED);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        return Result.success();
    }

    @Override
    public Result<Map<String, Object>> getCurrentUserInfo() {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) throw new BusinessException(UNAUTHORIZED, "请先登录");

        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(USER_NOT_EXIST);

        Map<String, Object> result = new HashMap<>();
        user.setPassword(null);
        result.put("user", user);
        return Result.success(result);
    }

    @Override
    public Result<Map<String, Object>> getUserByPhone(String phone) {
        User user = userMapper.selectByPhone(phone);
        if (user == null) return Result.success(null);
        Map<String, Object> result = new HashMap<>();
        user.setPassword(null);
        result.put("id", user.getId());
        result.put("phone", user.getPhone());
        result.put("name", user.getName());
        result.put("age", user.getAge());
        result.put("sex", user.getSex());
        result.put("userType", user.getUserType());
        result.put("status", user.getStatus());
        return Result.success(result);
    }

    @Override
    public Result<Map<String, Object>> getUserById(Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) return Result.success(null);
        Map<String, Object> result = new HashMap<>();
        user.setPassword(null);
        result.put("id", user.getId());
        result.put("phone", user.getPhone());
        result.put("name", user.getName());
        result.put("age", user.getAge());
        result.put("sex", user.getSex());
        result.put("userType", user.getUserType());
        result.put("status", user.getStatus());
        return Result.success(result);
    }

    /**
     * 获取或创建老人的 Elder 档案 ID（登录时兜底）
     * 先查询，查不到则自动创建，解决旧账号未建档案的问题
     */
    private Integer getOrCreateElder(Integer userId) {
        try {
            Result<Elder> elderResult = userFeignClient.getElderByUserId(userId);
            if (elderResult != null && elderResult.getData() != null) {
                return elderResult.getData().getId();
            }
        } catch (Exception e) {
            log.warn("查询Elder档案失败，尝试自动创建，userId: {}, 原因: {}", userId, e.getMessage());
        }
        // 兜底创建
        try {
            Result<Elder> createResult = userFeignClient.create(userId);
            if (createResult != null && createResult.getData() != null) {
                log.info("自动创建Elder档案成功，userId: {}, elderId: {}", userId, createResult.getData().getId());
                return createResult.getData().getId();
            }
        } catch (Exception ex) {
            log.error("自动创建Elder档案也失败，userId: {}, 错误: {}", userId, ex.getMessage());
        }
        return null;
    }

    private void validateSmsCode(String phone, String inputCode) {
        String stored = redisTemplate.opsForValue().get(SMS_CODE_PREFIX + phone);
        if (stored == null) throw new BusinessException(SMS_CODE_EXPIRED);
        if (!stored.equals(inputCode)) throw new BusinessException(SMS_CODE_ERROR);
    }
}

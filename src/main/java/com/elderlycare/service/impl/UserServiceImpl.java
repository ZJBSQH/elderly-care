package com.elderlycare.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.common.Result;
import com.elderlycare.common.exception.BusinessException;
import com.elderlycare.common.exception.ErrorCode;
import com.elderlycare.common.util.JwtUtil;
import com.elderlycare.common.util.QRCodeUtil;
import com.elderlycare.common.util.SecurityUtil;
import com.elderlycare.mapper.ElderMapper;
import com.elderlycare.mapper.FamilyMapper;
import com.elderlycare.mapper.UserMapper;
import com.elderlycare.pojo.dto.*;
import com.elderlycare.pojo.dto.auth.*;
import com.elderlycare.pojo.entity.Elder;
import com.elderlycare.pojo.entity.Family;
import com.elderlycare.pojo.entity.User;
import com.elderlycare.pojo.vo.UserVO;
import com.elderlycare.service.UserService;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类（无密码强度校验版）
 * 优化点：常量提取、重复逻辑抽取、异常规范化、依赖注入优化、日志增强
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // 常量定义
    private static final String SMS_CODE_PREFIX = "sms:code:"; // 短信验证码缓存前缀
    private static final Duration SMS_CODE_EXPIRE = Duration.ofMinutes(5); // 短信验证码有效期
    private static final Integer USER_TYPE_ELDER = 0; // 老人
    private static final Integer USER_TYPE_FAMILY = 1; // 家属
    private static final Integer USER_STATUS_ENABLE = 1; // 启用
    private static final String DEFAULT_RELATION = "亲属";
    private static final int PHONE_MASK_START = 3;
    private static final int PHONE_MASK_END = 7;

    private final UserMapper userMapper;
    private final ElderMapper elderMapper;
    private final FamilyMapper familyMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    // JWT工具类
    private final JwtUtil jwtUtil;
    //安全上下文工具，用于获取当前登录用户信息
    private final SecurityUtil securityUtil;

    // ==================== 核心业务方法 ====================

    /**
     * 发送验证码
     */
    @Override
    public Result<Void> sendSmsCode(SmsCodeRequest request) {
        // 1. 入参非空校验
        if (request.getPhone() == null || request.getType() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        String phone = request.getPhone();
        String type = request.getType();

        // 2. 根据类型校验用户存在性
        User existUser = userMapper.selectByPhone(phone);
        validateUserExistenceByType(type, existUser);

        // 3. 生成并缓存验证码
        String smsCode = RandomUtil.randomNumbers(6);
        redisTemplate.opsForValue().set(SMS_CODE_PREFIX + phone, smsCode, SMS_CODE_EXPIRE);
        log.info("为手机号{}生成{}类型验证码：{}", phone, type, smsCode);

        return Result.success();
    }

    /**
     * 注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<UserVO> register(RegisterRequest request) {
        // 1. 入参非空校验
        if (request.getPhone() == null || request.getSmsCode() == null || request.getPassword() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        String phone = request.getPhone();

        // 2. 校验验证码 + 手机号重复（防并发）
        validateSmsCode(phone, request.getSmsCode());
        if (userMapper.selectByPhone(phone) != null) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_REGISTERED);
        }

        // 3. 用户类型处理
        Integer userType = handleUserType(request);

        // 4. 构建并保存用户
        User user = buildUserFromRegisterRequest(request, userType);
        userMapper.insert(user);
        log.info("用户{}注册成功，ID：{}", phone, user.getId());

        // 5. 老人创建健康档案
        if (USER_TYPE_ELDER.equals(userType)) {
            createElderIfNotExist(user);
        }

        // 6. 删除验证码，返回 VO
        deleteSmsCode(phone);
        return Result.success(toUserVO(user));
    }

    /**
     * 登录
     */
    @Override
    public Result<Map<String, Object>> login(LoginRequest request) {
        // 1. 入参非空校验
        if (request.getPhone() == null || request.getPassword() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 2. 校验用户存在性 + 密码正确性
        User user = getUserByPhoneAndCheckExist(request.getPhone());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("用户{}登录失败：密码错误", request.getPhone());
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 3. 生成Token并构建返回数据
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getUserType());
        log.info("用户{}登录成功", request.getPhone());

        // 4. 构建返回数据
        Map<String, Object> response = buildUserInfoMap(user);
        response.put("token", token);


        return Result.success(response);
    }

    /**
     * 根据手机号查询用户
     */
    @Override
    public User findByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    /**
     * 保存用户信息
     */
    @Override
    public void saveUser(User user) {
        userMapper.insert(user);
    }

    /**
     * 修改用户信息
     */
    @Override
    public Result<UserVO> updateProfile(ProfileUpdateRequest request) {
        // 1. 入参非空校验
        if (request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 2. 查询并校验用户存在性
        User user = userMapper.selectById(request.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        // 3. 更新基本信息
        updateUserBasicInfo(user, request);
        userMapper.updateById(user);

        // 4. 老人确保健康档案存在
        if (USER_TYPE_ELDER.equals(user.getUserType())) {
            createElderIfNotExist(user);
        }

        log.info("用户{}（ID：{}）信息更新成功", user.getPhone(), user.getId());
        return Result.success(toUserVO(user));
    }

    /**
     * 重置密码（忘记密码）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> resetPassword(PasswordResetRequest request) {
        // 1. 入参非空校验
        if (request.getPhone() == null || request.getSmsCode() == null || request.getNewPassword() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        String phone = request.getPhone();

        // 2. 校验用户存在性 + 验证码
        User user = getUserByPhoneAndCheckExist(phone);
        validateSmsCode(phone, request.getSmsCode());

        // 3. 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        log.info("用户{}密码重置成功", phone);

        // 4. 删除验证码
        deleteSmsCode(phone);

        return Result.success();
    }

    /**
     * 修改密码（原密码验证）
     */
    @Override
    public Result<Void> changePassword(PasswordChangeRequest request) {
        // 1. 入参非空校验
        if (request.getPhone() == null || request.getOldPassword() == null || request.getNewPassword() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 2. 校验用户存在性 + 原密码正确性
        User user = getUserByPhoneAndCheckExist(request.getPhone());
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("用户{}修改密码失败：原密码错误", request.getPhone());
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 3. 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        log.info("用户{}密码修改成功", request.getPhone());

        return Result.success();
    }

    /**
     * 绑定家人（手机号方式）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> bindFamily(FamilyBindRequest request) {
        // 1. 入参非空校验
        if (request.getFamilyPhone() == null || request.getElderPhone() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        String familyPhone = request.getFamilyPhone();
        String elderPhone = request.getElderPhone();

        // 2. 校验家属用户（存在性+类型）
        User familyUser = getUserByPhoneAndCheckExist(familyPhone);
        checkUserType(familyUser, USER_TYPE_FAMILY, ErrorCode.NOT_FAMILY_USER);

        // 3. 校验老人用户（存在性+类型）
        User elderUser = getUserByPhoneAndCheckExist(elderPhone);
        checkUserType(elderUser, USER_TYPE_ELDER, ErrorCode.NOT_ELDER_USER);

        // 4. 校验老人健康档案
        Elder elder = elderMapper.selectOne(new LambdaQueryWrapper<Elder>().eq(Elder::getUserId, elderUser.getId()));
        if (elder == null) {
            throw new BusinessException(ErrorCode.ELDER_NOT_EXIST);
        }

        // 5. 校验是否已绑定
        if (checkIfAlreadyBound(familyUser.getId(), elder.getId())) {
            throw new BusinessException(ErrorCode.FAMILY_BIND_FAILED, "已绑定该老人");
        }

        // 6. 保存绑定关系
        Family family = buildFamilyFromBindRequest(familyUser, elder);
        familyMapper.insert(family);
        log.info("家属{}绑定老人{}成功", familyPhone, elderPhone);

        return Result.success();
    }

    /**
     * 老人生成专属二维码
     */
    @Override
    public Result<String> generateElderQRCode() {
        try {
            // 1. 校验登录状态 + 用户类型
            User currentUser = securityUtil.getCurrentUser();
            checkLoginStatus(currentUser);
            checkUserType(currentUser, USER_TYPE_ELDER, ErrorCode.NOT_ELDER_USER);

            // 2. 校验老人健康档案
            Elder elder = elderMapper.selectByUserId(currentUser.getId());
            if (elder == null) {
                throw new BusinessException(ErrorCode.ELDER_NOT_EXIST, "未找到健康档案");
            }

            // 3. 生成并保存二维码Token
            String qrCodeToken = IdUtil.fastSimpleUUID();
            elder.setQrCodeToken(qrCodeToken);
            elderMapper.updateById(elder);

            // 4. 生成Base64二维码
            String qrCodeBase64 = QRCodeUtil.generateElderQRCodeBase64(qrCodeToken);
            log.info("用户{}生成二维码成功", currentUser.getPhone());

            return Result.success(qrCodeBase64);
        } catch (Exception e) {
            log.error("生成二维码失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成二维码失败");
        }
    }

    /**
     * 家属扫描二维码获取老人信息
     */
    @Override
    public Result<ElderInfoDTO> parseQRCode(String qrCodeToken) {
        try {
            // 1. 入参非空校验 + 登录状态 + 用户类型
            if (qrCodeToken == null || qrCodeToken.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR);
            }
            User currentUser = securityUtil.getCurrentUser();
            checkLoginStatus(currentUser);
            checkUserType(currentUser, USER_TYPE_FAMILY, ErrorCode.NOT_FAMILY_USER);

            // 2. 校验二维码有效性
            Elder elder = elderMapper.selectByQrCodeToken(qrCodeToken);
            if (elder == null) {
                throw new BusinessException(ErrorCode.QR_CODE_INVALID);
            }

            // 3. 校验老人信息
            User elderUser = userMapper.selectById(elder.getUserId());
            if (elderUser == null) {
                throw new BusinessException(ErrorCode.USER_NOT_EXIST, "老人信息不存在");
            }

            // 4. 构建返回DTO（脱敏）
            boolean alreadyBound = checkIfAlreadyBound(currentUser.getId(), elder.getId());
            ElderInfoDTO elderInfo = buildElderInfo(elderUser, elder, alreadyBound);
            log.info("家属{}解析二维码成功，获取老人{}信息", currentUser.getPhone(), elderUser.getPhone());

            return Result.success(elderInfo);
        } catch (Exception e) {
            log.error("解析二维码失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析二维码失败");
        }
    }

    /**
     * 家属绑定老人（简化版，二维码确认）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> bindElderSimple(FamilyBindConfirmRequest request) {
        try {
            // 1. 入参非空校验 + 登录状态 + 用户类型
            if (request.getElderId() == null || request.getRelation() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR);
            }
            User familyUser = securityUtil.getCurrentUser();
            checkLoginStatus(familyUser);
            checkUserType(familyUser, USER_TYPE_FAMILY, ErrorCode.NOT_FAMILY_USER);

            // 2. 解析并校验老人ID
            Integer elderId = request.getElderId();
            Elder elder = elderMapper.selectById(elderId);
            if (elder == null) {
                throw new BusinessException(ErrorCode.ELDER_NOT_EXIST, "老人健康档案不存在");
            }

            // 3. 校验是否已绑定
            if (checkIfAlreadyBound(familyUser.getId(), elderId)) {
                throw new BusinessException(ErrorCode.FAMILY_BIND_FAILED, "已绑定该老人");
            }

            // 4. 保存绑定关系
            Family family = buildFamilyFromConfirmRequest(familyUser, elderId, request.getRelation());
            familyMapper.insert(family);

            User elderUser = userMapper.selectById(elder.getUserId());
            log.info("家属{}绑定老人{}成功，关系：{}",
                    familyUser.getPhone(),
                    elderUser != null ? elderUser.getPhone() : "未知",
                    request.getRelation());

            return Result.success();
        } catch (Exception e) {
            log.error("绑定老人失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "绑定老人失败");
        }
    }

    // ==================== 私有辅助方法（抽取重复逻辑） ====================

    /**
     * 根据验证码类型校验用户存在性
     */
    private void validateUserExistenceByType(String type, User existUser) {
        switch (type) {
            case "register":
                if (existUser != null) throw new BusinessException(ErrorCode.PHONE_ALREADY_REGISTERED);
                break;
            case "login":
                if (existUser == null) throw new BusinessException(ErrorCode.PHONE_NOT_REGISTERED);
                break;
            case "reset":
                if (existUser == null) throw new BusinessException(ErrorCode.USER_NOT_EXIST);
                break;
            default:
                throw new BusinessException(ErrorCode.PARAM_ERROR, "验证码类型仅支持register/login/reset");
        }
    }

    /**
     * 校验短信验证码
     */
    private void validateSmsCode(String phone, String inputSmsCode) {
        String redisKey = SMS_CODE_PREFIX + phone;
        String storedSmsCode = redisTemplate.opsForValue().get(redisKey);
        if (storedSmsCode == null) {
            throw new BusinessException(ErrorCode.SMS_CODE_EXPIRED);
        }
        if (!storedSmsCode.equals(inputSmsCode)) {
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }
    }

    /**
     * 删除Redis中的短信验证码
     */
    private void deleteSmsCode(String phone) {
        String redisKey = SMS_CODE_PREFIX + phone;
        redisTemplate.delete(redisKey);
    }

    /**
     * 根据手机号查询用户并校验存在性
     */
    private User getUserByPhoneAndCheckExist(String phone) {
        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            throw new BusinessException(ErrorCode.PHONE_NOT_REGISTERED);
        }
        return user;
    }

    /**
     * 校验用户类型
     */
    private void checkUserType(User user, Integer expectedType, ErrorCode errorCode) {
        if (!expectedType.equals(user.getUserType())) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 处理用户类型（自动分配/校验）
     */
    private Integer handleUserType(RegisterRequest request) {
        Integer userType = request.getUserType();
        Integer age = request.getAge();

        if (age == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "年龄不能为空");
        }

        if (userType == null) {
            userType = (age >= 60) ? USER_TYPE_ELDER : USER_TYPE_FAMILY;
            log.info("用户{}未指定类型，根据年龄{}自动分配为{}",
                    request.getPhone(), age,
                    userType == USER_TYPE_ELDER ? "老人" : "家属");
        } else if (!USER_TYPE_ELDER.equals(userType) && !USER_TYPE_FAMILY.equals(userType)) {
            throw new BusinessException(ErrorCode.USER_TYPE_INVALID);
        }
        return userType;
    }

    /**
     * 从注册请求构建用户对象
     */
    private User buildUserFromRegisterRequest(RegisterRequest request, Integer userType) {
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setSex(request.getSex());
        user.setAge(request.getAge());
        user.setUserType(userType);
        user.setStatus(USER_STATUS_ENABLE);
        user.setCreateTime(LocalDateTime.now());
        return user;
    }

    /**
     * 如果老人健康档案不存在则创建
     */
    private void createElderIfNotExist(User user) {
        Elder elder = elderMapper.selectOne(new LambdaQueryWrapper<Elder>().eq(Elder::getUserId, user.getId()));
        if (elder == null) {
            elder = new Elder();
            elder.setUserId(user.getId());
            elder.setMedicalHistory("");
            elder.setAllergicDrugs("");
            elder.setEmergencyContact("");
            elder.setHealthFile("");
            elderMapper.insert(elder);
            log.info("为用户{}创建健康档案", user.getPhone());
        }
    }

    /**
     * 更新用户基本信息
     */
    private void updateUserBasicInfo(User user, ProfileUpdateRequest request) {
        user.setName(request.getName());
        user.setAvatar(request.getAvatar());
        user.setAge(request.getAge());
        user.setSex(request.getSex());
    }

    /**
     * 从绑定请求构建Family对象
     */
    private Family buildFamilyFromBindRequest(User familyUser, Elder elder) {
        Family family = new Family();
        family.setFamilyUserId(familyUser.getId());
        family.setElderId(elder.getId());
        family.setBindStatus(USER_STATUS_ENABLE);
        family.setRelation(DEFAULT_RELATION);
        family.setPhone(familyUser.getPhone());
        family.setBindTime(LocalDateTime.now());
        return family;
    }

    /**
     * 校验登录状态
     */
    private void checkLoginStatus(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }
    }

    /**
     * 检查是否已经绑定过该老人
     */
    private boolean checkIfAlreadyBound(Integer familyUserId, Integer elderId) {
        Family existingFamily = familyMapper.selectOne(new LambdaQueryWrapper<Family>()
                .eq(Family::getFamilyUserId, familyUserId)
                .eq(Family::getElderId, elderId));
        return existingFamily != null;
    }

    /**
     * 构建老人信息DTO（脱敏）
     */
    private ElderInfoDTO buildElderInfo(User elderUser, Elder elder, boolean alreadyBound) {
        ElderInfoDTO dto = new ElderInfoDTO();
        dto.setElderId(elder.getId());
        dto.setName(elderUser.getName());
        dto.setAge(elderUser.getAge());
        dto.setSex(elderUser.getSex());
        dto.setPhone(maskPhone(elderUser.getPhone()));
        dto.setHasBound(alreadyBound);
        return dto;
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < PHONE_MASK_END) return phone;
        return phone.substring(0, PHONE_MASK_START) + "****" + phone.substring(PHONE_MASK_END);
    }


    /**
     * 从确认绑定请求构建Family对象
     */
    private Family buildFamilyFromConfirmRequest(User familyUser, Integer elderId, String relation) {
        Family family = new Family();
        family.setFamilyUserId(familyUser.getId());
        family.setElderId(elderId);
        family.setBindStatus(USER_STATUS_ENABLE);
        family.setRelation(relation);
        family.setPhone(familyUser.getPhone());
        family.setBindTime(LocalDateTime.now());
        return family;
    }
    
    /**
     * 获取当前登录用户信息
     */
    @Override
    public Result<Map<String, Object>> getCurrentUserInfo() {
        try {
            // 从 SecurityContext 获取当前用户
            User currentUser = securityUtil.getCurrentUser();
            if (currentUser == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
            }

            // 查询完整的用户信息
            User user = userMapper.selectById(currentUser.getId());
            if (user == null) {
                throw new BusinessException(ErrorCode.USER_NOT_EXIST);
            }

            log.info("获取用户信息成功，userId: {}", user.getId());
            return Result.success(buildUserInfoMap(user));
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取用户信息失败");
        }
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    // 构建用户信息
    private Map<String, Object> buildUserInfoMap(User user) {
        Map<String, Object> map = new HashMap<>();

        // 清除密码字段，不返回给前端
        user.setPassword(null);
        map.put("user", user);

        // 如果是老人用户，查询 elder 表获取扩展信息
        if (USER_TYPE_ELDER.equals(user.getUserType())) {
            Elder elder = elderMapper.selectByUserId(user.getId());
            if (elder != null) {
                map.put("elderId", elder.getId());
                map.put("emergencyContact", elder.getEmergencyContact());
                log.info("老人用户，elderId: {}, emergencyContact: {}",
                        elder.getId(), elder.getEmergencyContact());
            }
        }

        return map;
    }

}

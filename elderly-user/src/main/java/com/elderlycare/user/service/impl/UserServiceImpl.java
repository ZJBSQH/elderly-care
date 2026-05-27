package com.elderlycare.user.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.common.core.exception.BusinessException;
import com.elderlycare.common.core.result.Result;
import com.elderlycare.common.security.util.SecurityUtil;
import com.elderlycare.user.dto.ElderInfoDTO;
import com.elderlycare.user.dto.FamilyBindConfirmRequest;
import com.elderlycare.user.dto.FamilyBindRequest;
import com.elderlycare.user.entity.Elder;
import com.elderlycare.user.entity.Family;
import com.elderlycare.user.feign.AuthFeignClient;
import com.elderlycare.user.mapper.ElderMapper;
import com.elderlycare.user.mapper.FamilyMapper;
import com.elderlycare.user.service.UserService;
import com.elderlycare.user.util.QRCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.elderlycare.common.core.exception.BaseErrorCode.*;
import static com.elderlycare.user.dto.UserErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Integer USER_TYPE_ELDER = 0;
    private static final Integer USER_TYPE_FAMILY = 1;
    private static final Integer BIND_STATUS_ACTIVE = 1;
    private static final int PHONE_MASK_START = 3;
    private static final int PHONE_MASK_END = 7;

    private final ElderMapper elderMapper;
    private final FamilyMapper familyMapper;
    private final SecurityUtil securityUtil;
    private final AuthFeignClient authFeignClient;

    @Override
    public Result<String> generateElderQRCode() {
        Integer userId = getCurrentUserIdOrThrow();

        // 校验老人类型（通过auth服务查询）
        Map<String, Object> userInfo = authFeignClient.getUserById(userId).getData();
        if (userInfo == null || userInfo.get("userType") == null) {
            throw new BusinessException(UNAUTHORIZED, "用户信息获取失败");
        }
        if (!USER_TYPE_ELDER.equals(userInfo.get("userType"))) {
            throw new BusinessException(PARAM_ERROR, "仅老人用户可生成二维码");
        }

        Elder elder = getOrCreateElder(userId);
        String qrCodeToken = IdUtil.fastSimpleUUID();
        elder.setQrCodeToken(qrCodeToken);
        elderMapper.updateById(elder);

        String qrCodeBase64 = QRCodeUtil.generateElderQRCodeBase64(qrCodeToken);
        log.info("用户ID{}生成二维码成功", userId);
        return Result.success(qrCodeBase64);
    }

    @Override
    public Result<ElderInfoDTO> parseQRCode(String qrCodeToken) {
        Integer userId = getCurrentUserIdOrThrow();

        // 校验家属类型
        Map<String, Object> familyInfo = authFeignClient.getUserById(userId).getData();
        if (!USER_TYPE_FAMILY.equals(familyInfo.get("userType"))) {
            throw new BusinessException(PARAM_ERROR, "仅家属用户可扫描二维码");
        }

        Elder elder = elderMapper.selectByQrCodeToken(qrCodeToken);
        if (elder == null) {
            throw new BusinessException(QR_CODE_INVALID);
        }

        Map<String, Object> elderUserInfo = authFeignClient.getUserById(elder.getUserId()).getData();
        if (elderUserInfo == null) {
            throw new BusinessException(ELDER_NOT_EXIST, "老人信息不存在");
        }

        boolean alreadyBound = familyMapper.existsBinding(userId, elder.getId());

        ElderInfoDTO dto = new ElderInfoDTO();
        dto.setElderId(elder.getId());
        dto.setName((String) elderUserInfo.get("name"));
        dto.setAge((Integer) elderUserInfo.get("age"));
        dto.setSex((String) elderUserInfo.get("sex"));
        dto.setPhone(maskPhone((String) elderUserInfo.get("phone")));
        dto.setHasBound(alreadyBound);

        log.info("用户ID{}解析二维码成功，获取老人ID{}信息", userId, elder.getUserId());
        return Result.success(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> bindFamily(FamilyBindRequest request) {
        // 通过auth服务校验家属
        Map<String, Object> familyInfo = authFeignClient.getUserByPhone(request.getFamilyPhone()).getData();
        if (familyInfo == null) {
            throw new BusinessException(PARAM_ERROR, "家属手机号未注册");
        }
        if (!USER_TYPE_FAMILY.equals(familyInfo.get("userType"))) {
            throw new BusinessException(PARAM_ERROR, "该用户不是家属类型");
        }
        Integer familyUserId = (Integer) familyInfo.get("id");

        // 通过auth服务校验老人
        Map<String, Object> elderInfo = authFeignClient.getUserByPhone(request.getElderPhone()).getData();
        if (elderInfo == null) {
            throw new BusinessException(PARAM_ERROR, "老人手机号未注册");
        }
        if (!USER_TYPE_ELDER.equals(elderInfo.get("userType"))) {
            throw new BusinessException(PARAM_ERROR, "该用户不是老人类型");
        }
        Integer elderUserId = (Integer) elderInfo.get("id");

        Elder elder = elderMapper.selectByUserId(elderUserId);
        if (elder == null) {
            throw new BusinessException(ELDER_NOT_EXIST);
        }

        if (familyMapper.existsBinding(familyUserId, elder.getId())) {
            throw new BusinessException(FAMILY_ALREADY_BOUND);
        }

        Family family = new Family();
        family.setFamilyUserId(familyUserId);
        family.setElderId(elder.getId());
        family.setBindStatus(BIND_STATUS_ACTIVE);
        family.setRelation("亲属");
        family.setPhone(request.getFamilyPhone());
        family.setBindTime(LocalDateTime.now());
        familyMapper.insert(family);

        log.info("家属{}绑定老人{}成功", request.getFamilyPhone(), request.getElderPhone());
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> bindElderByQRCode(FamilyBindConfirmRequest request) {
        Integer userId = getCurrentUserIdOrThrow();

        Elder elder = elderMapper.selectById(request.getElderId());
        if (elder == null) {
            throw new BusinessException(ELDER_NOT_EXIST, "老人健康档案不存在");
        }

        if (familyMapper.existsBinding(userId, request.getElderId())) {
            throw new BusinessException(FAMILY_ALREADY_BOUND);
        }

        Map<String, Object> userInfo = authFeignClient.getUserById(userId).getData();

        Family family = new Family();
        family.setFamilyUserId(userId);
        family.setElderId(request.getElderId());
        family.setBindStatus(BIND_STATUS_ACTIVE);
        family.setRelation(request.getRelation());
        family.setPhone((String) userInfo.get("phone"));
        family.setBindTime(LocalDateTime.now());
        familyMapper.insert(family);

        log.info("家属ID{}绑定老人ID{}成功，关系：{}", userId, request.getElderId(), request.getRelation());
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> unbindFamily(Integer elderId) {
        Integer userId = getCurrentUserIdOrThrow();

        Family family = familyMapper.selectOne(new LambdaQueryWrapper<Family>()
                .eq(Family::getFamilyUserId, userId)
                .eq(Family::getElderId, elderId)
                .eq(Family::getBindStatus, BIND_STATUS_ACTIVE));

        if (family == null) {
            throw new BusinessException(FAMILY_NOT_BOUND);
        }

        familyMapper.deleteById(family.getId());
        log.info("家属ID{}解绑老人ID{}成功", userId, elderId);
        return Result.success();
    }

    @Override
    public Result<List<Map<String, Object>>> getBoundElders() {
        Integer userId = getCurrentUserIdOrThrow();

        List<Family> families = familyMapper.selectByFamilyUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Family f : families) {
            Elder elder = elderMapper.selectById(f.getElderId());
            if (elder == null) continue;
            Map<String, Object> userInfo = authFeignClient.getUserById(elder.getUserId()).getData();
            if (userInfo == null) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("elderId", elder.getId());
            item.put("userId", elder.getUserId());
            item.put("name", userInfo.get("name"));
            item.put("phone", maskPhone((String) userInfo.get("phone")));
            item.put("age", userInfo.get("age"));
            item.put("relation", f.getRelation());
            item.put("bindTime", f.getBindTime());
            result.add(item);
        }
        return Result.success(result);
    }

    @Override
    public Result<List<Map<String, Object>>> getBoundFamilyMembers(Integer elderId) {
        List<Family> families = familyMapper.selectByElderId(elderId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Family f : families) {
            Map<String, Object> userInfo = authFeignClient.getUserById(f.getFamilyUserId()).getData();
            if (userInfo == null) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("familyUserId", f.getFamilyUserId());
            item.put("name", userInfo.get("name"));
            item.put("phone", maskPhone((String) userInfo.get("phone")));
            item.put("relation", f.getRelation());
            item.put("bindTime", f.getBindTime());
            result.add(item);
        }
        return Result.success(result);
    }


    private Integer getCurrentUserIdOrThrow() {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(UNAUTHORIZED, "请先登录");
        }
        return userId;
    }

    private Elder getOrCreateElder(Integer userId) {
        Elder elder = elderMapper.selectByUserId(userId);
        if (elder == null) {
            elder = new Elder();
            elder.setUserId(userId);
            elder.setMedicalHistory("");
            elder.setAllergicDrugs("");
            elder.setEmergencyContact("");
            elder.setHealthFile("");
            elderMapper.insert(elder);
            log.info("为用户ID{}创建健康档案", userId);
        }
        return elder;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < PHONE_MASK_END) return phone;
        return phone.substring(0, PHONE_MASK_START) + "****" + phone.substring(PHONE_MASK_END);
    }
}

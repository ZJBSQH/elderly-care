package com.elderlycare.user.service;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.user.dto.ElderInfoDTO;
import com.elderlycare.user.dto.FamilyBindConfirmRequest;
import com.elderlycare.user.dto.FamilyBindRequest;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 为当前老人用户生成二维码（Base64格式）
     */
    Result<String> generateElderQRCode();

    /**
     * 家属扫描二维码解析老人信息
     */
    Result<ElderInfoDTO> parseQRCode(String qrCodeToken);

    /**
     * 家属通过手机号绑定老人
     */
    Result<Void> bindFamily(FamilyBindRequest request);

    /**
     * 家属通过二维码确认绑定老人
     */
    Result<Void> bindElderByQRCode(FamilyBindConfirmRequest request);

    /**
     * 家属解绑老人
     */
    Result<Void> unbindFamily(Integer elderId);

    /**
     * 获取当前家属已绑定的老人列表
     */
    Result<List<Map<String, Object>>> getBoundElders();

    /**
     * 获取指定老人已绑定的家属列表
     */
    Result<List<Map<String, Object>>> getBoundFamilyMembers(Integer elderId);

}

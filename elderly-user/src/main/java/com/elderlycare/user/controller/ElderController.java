package com.elderlycare.user.controller;

import com.elderlycare.common.core.exception.BusinessException;
import com.elderlycare.common.core.result.Result;
import com.elderlycare.user.dto.ElderInfoDTO;
import com.elderlycare.user.entity.Elder;
import com.elderlycare.user.mapper.ElderMapper;
import com.elderlycare.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 老人健康档案控制器
 */
@RestController
@RequestMapping("/user/elder")
@RequiredArgsConstructor
public class ElderController {

    private final UserService userService;
    private final ElderMapper elderMapper;

    /**
     * 根据认证用户ID查询老人档案（供认证模块调用）
     */
    @GetMapping("/byUserId")
    public Result<Elder> getElderByUserId(@RequestParam Integer userId) {
        Elder elder = elderMapper.selectByUserId(userId);
        if (elder == null) {
            throw new BusinessException("老人档案不存在");
        }
        return Result.success(elder);
    }

    /**
     * 创建老人健康档案（幂等：已存在则直接返回）
     */
    @PostMapping("/create")
    public Result<Elder> createElder(@RequestParam Integer userId) {
        Elder elder = elderMapper.selectByUserId(userId);
        if (elder != null) {
            return Result.success(elder);
        }
        Elder newElder = new Elder();
        newElder.setUserId(userId);
        elderMapper.insert(newElder);
        return Result.success(newElder);
    }

    /**
     * 生成老人绑定二维码（Base64格式）
     */
    @GetMapping("/qrcode/generate")
    public Result<String> generateQRCode() {
        return userService.generateElderQRCode();
    }

    /**
     * 家属扫描解析老人二维码信息
     */
    @GetMapping("/qrcode/parse")
    public Result<ElderInfoDTO> parseQRCode(@RequestParam String qrCodeToken) {
        return userService.parseQRCode(qrCodeToken);
    }

    /**
     * 获取指定老人已绑定的家属成员列表
     */
    @GetMapping("/families")
    public Result<?> getBoundFamilyMembers(@RequestParam Integer elderId) {
        return userService.getBoundFamilyMembers(elderId);
    }
}

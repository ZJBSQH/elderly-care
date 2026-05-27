package com.elderlycare.user.controller;

import com.elderlycare.common.core.exception.BusinessException;
import com.elderlycare.common.core.result.Result;
import com.elderlycare.user.dto.ElderInfoDTO;
import com.elderlycare.user.entity.Elder;
import com.elderlycare.user.mapper.ElderMapper;
import com.elderlycare.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/elder")
@RequiredArgsConstructor
public class ElderController {

    private final UserService userService;
    private final ElderMapper elderMapper;

    //传递老人id给认证模块
    @GetMapping("/byUserId")
    public Result<Elder> getElderByUserId(@RequestParam Integer userId) {
        Elder elder = elderMapper.selectByUserId(userId);
        if (elder == null) {
            throw new BusinessException("老人档案不存在");
        }
        return Result.success(elder);
    }

    //创建老人档案
    @PostMapping("/create")
    public Result<Elder> createElder(@RequestParam Integer userId) {
        Elder elder = elderMapper.selectByUserId(userId);
        if (elder != null) {
            return Result.success(elder);  // 已存在，直接返回（幂等）
        }
        // 不存在，新建
        Elder newElder = new Elder();
        newElder.setUserId(userId);
        elderMapper.insert(newElder);
        return Result.success(newElder);  // 插入后 MyBatis-Plus 自动回填 id
    }

    @GetMapping("/qrcode/generate")
    public Result<String> generateQRCode() {
        return userService.generateElderQRCode();
    }

    @GetMapping("/qrcode/parse")
    public Result<ElderInfoDTO> parseQRCode(@RequestParam String qrCodeToken) {
        return userService.parseQRCode(qrCodeToken);
    }

    @GetMapping("/families")
    public Result<?> getBoundFamilyMembers(@RequestParam Integer elderId) {
        return userService.getBoundFamilyMembers(elderId);
    }
}

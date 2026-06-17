package com.elderlycare.controller.admin;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.dto.admin.SystemConfigDTO;
import com.elderlycare.pojo.vo.admin.SystemConfigVO;
import com.elderlycare.service.SystemConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置管理控制器
 */
@RestController
@RequestMapping("/admin/system")
@RequiredArgsConstructor
public class AdminSystemController {

    private final SystemConfigService systemConfigService;

    /**
     * 查询所有系统配置
     */
    @GetMapping("/configs")
    public Result<List<SystemConfigVO>> getAllConfigs() {
        return Result.success(systemConfigService.getAllConfigs());
    }

    /**
     * 根据 Key 查询配置
     */
    @GetMapping("/configs/{key}")
    public Result<SystemConfigVO> getConfigByKey(@PathVariable String key) {
        return Result.success(systemConfigService.getConfigByKey(key));
    }

    /**
     * 批量更新系统配置
     */
    @PutMapping("/configs")
    public Result<Void> updateConfigs(@Valid @RequestBody List<SystemConfigDTO> configs) {
        boolean success = systemConfigService.batchUpdateConfigs(configs);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 更新单个配置
     */
    @PutMapping("/configs/{key}")
    public Result<Void> updateConfig(
            @PathVariable String key,
            @RequestParam String value
    ) {
        boolean success = systemConfigService.updateConfig(key, value);
        return success ? Result.success() : Result.error("更新失败");
    }
}

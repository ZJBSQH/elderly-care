package com.elderlycare.admin.controller;

import com.elderlycare.admin.dto.SystemConfigDTO;
import com.elderlycare.admin.service.SystemConfigService;
import com.elderlycare.admin.vo.SystemConfigVO;
import com.elderlycare.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统配置管理控制器
 *
 * @author 郑
 */
@Slf4j
@RestController
@RequestMapping("/admin/system")
@RequiredArgsConstructor
public class AdminSystemController {

    private final SystemConfigService systemConfigService;

    /**
     * 获取所有系统配置
     *
     * @return 配置列表
     */
    @GetMapping("/configs")
    public Result<List<SystemConfigVO>> getAllConfigs() {
        log.info("获取所有系统配置请求");
        return systemConfigService.getAllConfigs();
    }

    /**
     * 根据键获取配置
     *
     * @param key 配置键
     * @return 配置信息
     */
    @GetMapping("/configs/{key}")
    public Result<SystemConfigVO> getConfigByKey(@PathVariable String key) {
        log.info("获取系统配置请求，key: {}", key);
        return systemConfigService.getConfigByKey(key);
    }

    /**
     * 更新单个配置
     *
     * @param key   配置键
     * @param value 配置值（请求体）
     * @return 更新结果
     */
    @PutMapping("/configs/{key}")
    public Result<Void> updateConfig(@PathVariable String key, @RequestBody String value) {
        log.info("更新系统配置请求，key: {}, value: {}", key, value);
        return systemConfigService.updateConfig(key, value);
    }

    /**
     * 批量更新配置
     *
     * @param configs 配置DTO列表
     * @return 更新结果
     */
    @PutMapping("/configs")
    public Result<Void> batchUpdateConfigs(@RequestBody List<SystemConfigDTO> configs) {
        log.info("批量更新系统配置请求，数量: {}", configs.size());
        return systemConfigService.batchUpdateConfigs(configs);
    }
}

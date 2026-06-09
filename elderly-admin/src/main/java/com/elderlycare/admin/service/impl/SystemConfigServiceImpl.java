package com.elderlycare.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.admin.dto.AdminErrorCode;
import com.elderlycare.admin.dto.SystemConfigDTO;
import com.elderlycare.admin.entity.SystemConfig;
import com.elderlycare.admin.mapper.SystemConfigMapper;
import com.elderlycare.admin.service.SystemConfigService;
import com.elderlycare.admin.vo.SystemConfigVO;
import com.elderlycare.common.core.exception.BusinessException;
import com.elderlycare.common.core.util.BeanUtil;
import com.elderlycare.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现
 *
 * @author 郑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    /**
     * 获取所有系统配置
     *
     * @return 配置列表
     */
    @Override
    public Result<List<SystemConfigVO>> getAllConfigs() {
        List<SystemConfig> configs = systemConfigMapper.selectList(null);
        List<SystemConfigVO> voList = configs.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 根据键获取系统配置
     *
     * @param key 配置键
     * @return 配置信息
     */
    @Override
    public Result<SystemConfigVO> getConfigByKey(String key) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, key));
        if (config == null) {
            throw new BusinessException(AdminErrorCode.CONFIG_NOT_FOUND);
        }
        return Result.success(toVO(config));
    }

    /**
     * 更新单个系统配置
     *
     * @param key   配置键
     * @param value 配置值
     * @return 更新结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateConfig(String key, String value) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, key));
        if (config == null) {
            throw new BusinessException(AdminErrorCode.CONFIG_NOT_FOUND);
        }
        config.setConfigValue(value);
        config.setUpdateTime(LocalDateTime.now());
        systemConfigMapper.updateById(config);
        log.info("更新系统配置成功，key: {}, value: {}", key, value);
        return Result.success(null, "系统配置更新成功");
    }

    /**
     * 批量更新系统配置
     *
     * @param configs 配置DTO列表
     * @return 更新结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> batchUpdateConfigs(List<SystemConfigDTO> configs) {
        // 批量查询所有需要的配置键
        List<String> keys = configs.stream()
                .map(SystemConfigDTO::getConfigKey)
                .collect(Collectors.toList());
        List<SystemConfig> existingConfigs = systemConfigMapper.selectList(
                new LambdaQueryWrapper<SystemConfig>()
                        .in(SystemConfig::getConfigKey, keys));
        Map<String, SystemConfig> configMap = existingConfigs.stream()
                .collect(Collectors.toMap(SystemConfig::getConfigKey, c -> c));

        // 批量更新
        for (SystemConfigDTO dto : configs) {
            SystemConfig config = configMap.get(dto.getConfigKey());
            if (config != null) {
                BeanUtil.copyNonNullProperties(dto, config);
                config.setUpdateTime(LocalDateTime.now());
                systemConfigMapper.updateById(config);
            } else {
                log.warn("系统配置键不存在，跳过更新: {}", dto.getConfigKey());
            }
        }
        log.info("批量更新系统配置成功，共处理 {} 条", configs.size());
        return Result.success(null, "批量更新系统配置成功");
    }

    /**
     * 将实体转换为视图对象
     *
     * @param config 系统配置实体
     * @return 系统配置VO
     */
    private SystemConfigVO toVO(SystemConfig config) {
        SystemConfigVO vo = new SystemConfigVO();
        BeanUtil.copyProperties(config, vo);
        return vo;
    }
}

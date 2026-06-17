package com.elderlycare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.mapper.SystemConfigMapper;
import com.elderlycare.pojo.dto.admin.SystemConfigDTO;
import com.elderlycare.pojo.entity.SystemConfig;
import com.elderlycare.pojo.vo.admin.SystemConfigVO;
import com.elderlycare.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统配置服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    @Override
    public List<SystemConfigVO> getAllConfigs() {
        List<SystemConfig> configs = systemConfigMapper.selectList(new LambdaQueryWrapper<>());
        return configs.stream()
            .map(this::convertToVO)
            .toList();
    }

    @Override
    public SystemConfigVO getConfigByKey(String configKey) {
        SystemConfig config = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, configKey)
        );
        if (config == null) {
            throw new RuntimeException("配置不存在：" + configKey);
        }
        return convertToVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateConfigs(List<SystemConfigDTO> configs) {
        if (configs == null || configs.isEmpty()) {
            return false;
        }

        for (SystemConfigDTO configDTO : configs) {
            updateConfig(configDTO.getConfigKey(), configDTO.getConfigValue());
        }

        log.info("批量更新系统配置成功，数量：{}", configs.size());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfig(String configKey, String configValue) {
        SystemConfig config = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, configKey)
        );

        if (config == null) {
            // 如果配置不存在，创建新配置
            config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            systemConfigMapper.insert(config);
            log.info("创建新配置成功，key: {}", configKey);
        } else {
            // 更新现有配置
            config.setConfigValue(configValue);
            systemConfigMapper.updateById(config);
            log.info("更新配置成功，key: {}", configKey);
        }

        return true;
    }

    /**
     * 转换为 VO
     */
    private SystemConfigVO convertToVO(SystemConfig config) {
        return new SystemConfigVO(
            config.getId(),
            config.getConfigKey(),
            config.getConfigValue(),
            config.getDescription(),
            config.getUpdateTime()
        );
    }
}

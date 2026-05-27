package com.elderlycare.admin.service;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.admin.dto.SystemConfigDTO;
import com.elderlycare.admin.vo.SystemConfigVO;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 *
 * @author 郑
 */
public interface SystemConfigService {

    /**
     * 获取所有系统配置
     *
     * @return 配置列表
     */
    Result<List<SystemConfigVO>> getAllConfigs();

    /**
     * 根据键获取配置
     *
     * @param key 配置键
     * @return 配置信息
     */
    Result<SystemConfigVO> getConfigByKey(String key);

    /**
     * 更新单个配置
     *
     * @param key   配置键
     * @param value 配置值
     * @return 更新结果
     */
    Result<Void> updateConfig(String key, String value);

    /**
     * 批量更新配置
     *
     * @param configs 配置DTO列表
     * @return 更新结果
     */
    Result<Void> batchUpdateConfigs(List<SystemConfigDTO> configs);
}

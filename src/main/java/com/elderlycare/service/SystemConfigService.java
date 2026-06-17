package com.elderlycare.service;

import com.elderlycare.pojo.dto.admin.SystemConfigDTO;
import com.elderlycare.pojo.vo.admin.SystemConfigVO;

import java.util.List;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService {

    /**
     * 查询所有系统配置
     *
     * @return 配置列表
     */
    List<SystemConfigVO> getAllConfigs();

    /**
     * 根据 Key 查询配置
     *
     * @param configKey 配置键
     * @return 配置信息
     */
    SystemConfigVO getConfigByKey(String configKey);

    /**
     * 批量更新系统配置
     *
     * @param configs 配置列表
     * @return 操作结果
     */
    boolean batchUpdateConfigs(List<SystemConfigDTO> configs);

    /**
     * 更新单个配置
     *
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 操作结果
     */
    boolean updateConfig(String configKey, String configValue);
}

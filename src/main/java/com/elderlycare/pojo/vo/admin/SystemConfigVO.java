package com.elderlycare.pojo.vo.admin;

import java.time.LocalDateTime;

/**
 * 系统配置 VO
 */
public record SystemConfigVO(
    /**
     * 主键 ID
     */
    Integer id,

    /**
     * 配置键
     */
    String configKey,

    /**
     * 配置值
     */
    String configValue,

    /**
     * 配置描述
     */
    String description,

    /**
     * 更新时间
     */
    LocalDateTime updateTime
) {}

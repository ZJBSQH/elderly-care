package com.elderlycare.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统配置 DTO
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigDTO {

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置描述
     */
    private String description;
}

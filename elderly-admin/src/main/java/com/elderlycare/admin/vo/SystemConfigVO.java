package com.elderlycare.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统配置 VO
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigVO {

    /**
     * 主键ID
     */
    private Long id;

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

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

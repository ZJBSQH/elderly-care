package com.elderlycare.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 健康预警视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthWarningVO {
    
    /**
     * 预警 ID
     */
    private Integer id;
    
    /**
     * 老人 ID
     */
    private Integer elderId;
    
    /**
     * 老人姓名
     */
    private String elderName;
    
    /**
     * 异常指标类型
     */
    private String abnormalType;
    
    /**
     * 异常详情描述
     */
    private String abnormalDetail;
    
    /**
     * 当前值
     */
    private String currentValue;
    
    /**
     * 正常范围
     */
    private String normalRange;
    
    /**
     * 预警时间
     */
    private LocalDateTime warningTime;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
}

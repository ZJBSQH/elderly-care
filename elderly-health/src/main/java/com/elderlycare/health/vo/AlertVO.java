package com.elderlycare.health.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 健康预警视图对象
 *
 * @author 郑
 */
@Data
public class AlertVO {

    /**
     * 记录ID（对应 health 表 id）
     */
    private Long id;

    /**
     * 老人ID
     */
    private Integer elderId;

    /**
     * 异常指标类型
     */
    private String abnormalType;

    /**
     * 异常详情描述
     */
    private String abnormalDetail;

    /**
     * 预警时间
     */
    private LocalDateTime warningTime;

    /**
     * 是否已读
     */
    private Boolean isRead;
}

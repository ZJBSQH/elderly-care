package com.elderlycare.medicine.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服药记录视图对象
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordVO {

    /**
     * 主键 ID
     */
    private Integer id;

    /**
     * 关联用药计划 ID
     */
    private Integer taskId;

    /**
     * 老人 ID
     */
    private Integer elderId;

    /**
     * 计划服药日期
     */
    private LocalDate remindDate;

    /**
     * 实际服药时间
     */
    private LocalDateTime recordTime;

    /**
     * 状态: 0-待服, 1-已服, 2-漏服, 3-跳过
     */
    private Integer status;

    /**
     * 药品名称（关联 medicine 表）
     */
    private String medicineName;

    /**
     * 剂量（关联 medicine 表）
     */
    private String dosage;

    /**
     * 频次（关联 medicine 表）
     */
    private String frequency;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 备注
     */
    private String remark;
}

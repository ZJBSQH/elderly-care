package com.elderlycare.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 健康数据统计 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatisticsVO {
    
    /**
     * 老人 ID
     */
    private Integer elderId;
    
    /**
     * 统计开始时间
     */
    private LocalDateTime startDate;
    
    /**
     * 统计结束时间
     */
    private LocalDateTime endDate;
    
    /**
     * 总记录数
     */
    private Integer totalRecords;
    
    /**
     * 异常记录数
     */
    private Integer abnormalRecords;
    
    /**
     * 正常记录数
     */
    private Integer normalRecords;
    
    /**
     * 异常率（百分比）
     */
    private BigDecimal abnormalRate;
    
    /**
     * 平均血压（收缩压）
     */
    private Integer avgSystolic;
    
    /**
     * 平均血压（舒张压）
     */
    private Integer avgDiastolic;
    
    /**
     * 平均血糖
     */
    private BigDecimal avgBloodSugar;
    
    /**
     * 平均心率
     */
    private Integer avgHeartRate;
    
    /**
     * 平均体重
     */
    private BigDecimal avgWeight;
    
    /**
     * 最高血压（收缩压）
     */
    private Integer maxSystolic;
    
    /**
     * 最高血压（舒张压）
     */
    private Integer maxDiastolic;
    
    /**
     * 最高血糖
     */
    private BigDecimal maxBloodSugar;
    
    /**
     * 最高心率
     */
    private Integer maxHeartRate;
    
    /**
     * 最高体重
     */
    private BigDecimal maxWeight;
    
    /**
     * 最低血压（收缩压）
     */
    private Integer minSystolic;
    
    /**
     * 最低血压（舒张压）
     */
    private Integer minDiastolic;
    
    /**
     * 最低血糖
     */
    private BigDecimal minBloodSugar;
    
    /**
     * 最低心率
     */
    private Integer minHeartRate;
    
    /**
     * 最低体重
     */
    private BigDecimal minWeight;
}

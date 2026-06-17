package com.elderlycare.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 健康趋势数据 VO
 * 用于前端绘制折线图
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthTrendVO {

    /**
     * 日期列表（X 轴）
     */
    private List<String> dates;

    /**
     * 血压数据（收缩压/舒张压）
     */
    private BloodPressureTrend bloodPressure;

    /**
     * 血糖数据
     */
    private MetricTrend bloodSugar;

    /**
     * 心率数据
     */
    private MetricTrend heartRate;

    /**
     * 体重数据
     */
    private MetricTrend weight;

    /**
     * 血压趋势（内部类）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BloodPressureTrend {
        /**
         * 收缩压数据
         */
        private List<Integer> systolic;

        /**
         * 舒张压数据
         */
        private List<Integer> diastolic;

        /**
         * 异常标记（对应日期的异常状态）
         */
        private List<Integer> warningFlags;
    }

    /**
     * 单一指标趋势（内部类）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricTrend<T extends Number> {
        /**
         * 指标数值
         */
        private List<T> values;

        /**
         * 异常标记
         */
        private List<Integer> warningFlags;
    }
}


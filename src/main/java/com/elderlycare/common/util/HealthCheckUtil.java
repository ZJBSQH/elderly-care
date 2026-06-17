package com.elderlycare.common.util;

import java.math.BigDecimal;

/**
 * 健康指标阈值检查工具类
 */
public class HealthCheckUtil {

    // 血压阈值
    public static final int SYSTOLIC_MAX = 140;
    public static final int SYSTOLIC_MIN = 90;
    public static final int DIASTOLIC_MAX = 90;
    public static final int DIASTOLIC_MIN = 60;

    // 血糖阈值
    public static final BigDecimal BLOOD_SUGAR_MAX = new BigDecimal("6.1");
    public static final BigDecimal BLOOD_SUGAR_MIN = new BigDecimal("3.9");

    // 心率阈值
    public static final int HEART_RATE_MAX = 100;
    public static final int HEART_RATE_MIN = 60;

    // 体重阈值
    public static final BigDecimal WEIGHT_MAX = new BigDecimal("150");
    public static final BigDecimal WEIGHT_MIN = new BigDecimal("40");

    /**
     * 解析血压字符串 "收缩压/舒张压"，返回 [systolic, diastolic]，解析失败返回 null
     */
    public static int[] parseBloodPressure(String bloodPressure) {
        if (bloodPressure == null || !bloodPressure.contains("/")) return null;
        try {
            String[] parts = bloodPressure.split("/");
            return new int[]{
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim())
            };
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isBloodPressureAbnormal(int systolic, int diastolic) {
        return systolic > SYSTOLIC_MAX || systolic < SYSTOLIC_MIN
                || diastolic > DIASTOLIC_MAX || diastolic < DIASTOLIC_MIN;
    }

    public static boolean isBloodSugarAbnormal(BigDecimal bloodSugar) {
        if (bloodSugar == null) return false;
        return bloodSugar.compareTo(BLOOD_SUGAR_MAX) > 0 || bloodSugar.compareTo(BLOOD_SUGAR_MIN) < 0;
    }

    public static boolean isHeartRateAbnormal(Integer heartRate) {
        if (heartRate == null) return false;
        return heartRate > HEART_RATE_MAX || heartRate < HEART_RATE_MIN;
    }

    public static boolean isWeightAbnormal(BigDecimal weight) {
        if (weight == null) return false;
        return weight.compareTo(WEIGHT_MAX) > 0 || weight.compareTo(WEIGHT_MIN) < 0;
    }

    /**
     * 综合检查：任一指标异常返回 true
     */
    public static boolean hasAnyAbnormal(String bloodPressure, BigDecimal bloodSugar,
                                         Integer heartRate, BigDecimal weight) {
        int[] bp = parseBloodPressure(bloodPressure);
        if (bp != null && isBloodPressureAbnormal(bp[0], bp[1])) return true;
        if (isBloodSugarAbnormal(bloodSugar)) return true;
        if (isHeartRateAbnormal(heartRate)) return true;
        if (isWeightAbnormal(weight)) return true;
        return false;
    }
}

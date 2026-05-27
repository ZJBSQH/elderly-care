package com.elderlycare.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.common.core.result.Result;
import com.elderlycare.health.dto.HealthDTO;
import com.elderlycare.health.dto.HealthErrorCode;
import com.elderlycare.health.entity.Health;
import com.elderlycare.health.mapper.HealthMapper;
import com.elderlycare.health.service.HealthService;
import com.elderlycare.health.vo.AlertVO;
import com.elderlycare.health.vo.HealthVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 健康记录服务实现
 *
 * @author 郑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthServiceImpl implements HealthService {

    private final HealthMapper healthMapper;

    /**
     * 收缩压正常范围下限
     */
    private static final int SYSTOLIC_MIN = 90;
    /**
     * 收缩压正常范围上限
     */
    private static final int SYSTOLIC_MAX = 140;
    /**
     * 舒张压正常范围下限
     */
    private static final int DIASTOLIC_MIN = 60;
    /**
     * 舒张压正常范围上限
     */
    private static final int DIASTOLIC_MAX = 90;
    /**
     * 血糖正常范围下限
     */
    private static final double BLOOD_SUGAR_MIN = 3.9;
    /**
     * 血糖正常范围上限
     */
    private static final double BLOOD_SUGAR_MAX = 6.1;
    /**
     * 心率正常范围下限
     */
    private static final int HEART_RATE_MIN = 60;
    /**
     * 心率正常范围上限
     */
    private static final int HEART_RATE_MAX = 100;
    /**
     * 体重正常范围下限
     */
    private static final double WEIGHT_MIN = 40.0;
    /**
     * 体重正常范围上限
     */
    private static final double WEIGHT_MAX = 150.0;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<HealthVO> saveHealth(HealthDTO healthDTO) {
        Health health = new Health();
        health.setElderId(healthDTO.getElderId());
        health.setBloodPressure(healthDTO.getBloodPressure());
        health.setBloodSugar(healthDTO.getBloodSugar());
        health.setHeartRate(healthDTO.getHeartRate());
        health.setWeight(healthDTO.getWeight());
        health.setRecordTime(LocalDateTime.now());

        // 检查各指标是否在正常范围内，设置预警标识
        boolean abnormal = isAbnormal(healthDTO);
        health.setWarningFlag(abnormal ? 1 : 0);

        healthMapper.insert(health);
        log.info("保存健康记录成功，elderId: {}, id: {}, warningFlag: {}",
                healthDTO.getElderId(), health.getId(), health.getWarningFlag());

        return Result.success(toVO(health), "健康记录保存成功");
    }

    @Override
    public Result<List<HealthVO>> getTodayRecords(Integer elderId) {
        List<Health> records = healthMapper.selectToday(elderId);
        List<HealthVO> voList = records.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    public Result<List<HealthVO>> getHistoryRecords(Integer elderId, LocalDateTime start, LocalDateTime end) {
        List<Health> records = healthMapper.selectByDateRange(elderId, start, end);
        List<HealthVO> voList = records.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    public Result<HealthVO> getLatestRecord(Integer elderId) {
        Health health = healthMapper.selectLatest(elderId);
        if (health == null) {
            return Result.success(null);
        }
        return Result.success(toVO(health));
    }

    @Override
    public Result<Map<String, Object>> getStatistics(Integer elderId) {
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        List<Health> records = healthMapper.selectByDateRange(elderId, threeMonthsAgo, LocalDateTime.now());

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", records.size());

        // 异常记录数
        long abnormalCount = records.stream()
                .filter(h -> h.getWarningFlag() != null && h.getWarningFlag() == 1)
                .count();
        stats.put("abnormalCount", abnormalCount);

        // 计算各项平均值
        double avgBloodSugar = records.stream()
                .filter(h -> h.getBloodSugar() != null)
                .mapToDouble(Health::getBloodSugar)
                .average()
                .orElse(0.0);
        double avgHeartRate = records.stream()
                .filter(h -> h.getHeartRate() != null)
                .mapToInt(Health::getHeartRate)
                .average()
                .orElse(0.0);
        double avgWeight = records.stream()
                .filter(h -> h.getWeight() != null)
                .mapToDouble(Health::getWeight)
                .average()
                .orElse(0.0);

        stats.put("avgBloodSugar", Math.round(avgBloodSugar * 100.0) / 100.0);
        stats.put("avgHeartRate", Math.round(avgHeartRate * 10.0) / 10.0);
        stats.put("avgWeight", Math.round(avgWeight * 100.0) / 100.0);

        return Result.success(stats);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<HealthVO> updateRecord(Long id, HealthDTO healthDTO) {
        Health existing = healthMapper.selectById(id);
        if (existing == null) {
            return Result.error(HealthErrorCode.HEALTH_RECORD_NOT_EXIST.getCode(),
                    HealthErrorCode.HEALTH_RECORD_NOT_EXIST.getMessage());
        }
        if (healthDTO.getBloodPressure() != null) existing.setBloodPressure(healthDTO.getBloodPressure());
        if (healthDTO.getBloodSugar() != null) existing.setBloodSugar(healthDTO.getBloodSugar());
        if (healthDTO.getHeartRate() != null) existing.setHeartRate(healthDTO.getHeartRate());
        if (healthDTO.getWeight() != null) existing.setWeight(healthDTO.getWeight());
        // 重新计算预警标识
        boolean abnormal = isAbnormal(healthDTO);
        existing.setWarningFlag(abnormal ? 1 : 0);
        healthMapper.updateById(existing);
        log.info("更新健康记录成功，id: {}, warningFlag: {}", id, existing.getWarningFlag());
        return Result.success(toVO(existing), "健康记录更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteRecord(Long id) {
        Health health = healthMapper.selectById(id);
        if (health == null) {
            return Result.error(HealthErrorCode.HEALTH_RECORD_NOT_EXIST.getCode(),
                    HealthErrorCode.HEALTH_RECORD_NOT_EXIST.getMessage());
        }
        healthMapper.deleteById(id);
        log.info("删除健康记录成功，id: {}", id);
        return Result.success(null, "健康记录删除成功");
    }

    @Override
    public Result<List<AlertVO>> getAlertList(Integer elderId) {
        List<Health> alerts = healthMapper.selectAlerts(elderId);
        List<AlertVO> voList = alerts.stream()
                .map(this::toAlertVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    public Result<Integer> getUnreadAlertCount(Integer elderId) {
        Long count = healthMapper.countUnreadAlerts(elderId);
        return Result.success(count.intValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> markAlertRead(Long id) {
        Health health = healthMapper.selectById(id);
        if (health == null) {
            return Result.error(HealthErrorCode.HEALTH_RECORD_NOT_EXIST.getCode(),
                    HealthErrorCode.HEALTH_RECORD_NOT_EXIST.getMessage());
        }
        health.setIsRead(1);
        healthMapper.updateById(health);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> markAllAlertsRead(Integer elderId) {
        List<Health> unreadList = healthMapper.selectUnreadAlerts(elderId);
        for (Health health : unreadList) {
            health.setIsRead(1);
            healthMapper.updateById(health);
        }
        log.info("老人 {} 的全部预警已标记已读，共 {} 条", elderId, unreadList.size());
        return Result.success();
    }

    /**
     * 判断健康指标是否异常
     *
     * @param dto 健康记录DTO
     * @return true-异常, false-正常
     */
    private boolean isAbnormal(HealthDTO dto) {
        // 检查血压
        if (dto.getBloodPressure() != null && !dto.getBloodPressure().isEmpty()) {
            try {
                String[] parts = dto.getBloodPressure().split("/");
                if (parts.length == 2) {
                    int systolic = Integer.parseInt(parts[0].trim());
                    int diastolic = Integer.parseInt(parts[1].trim());
                    if (systolic < SYSTOLIC_MIN || systolic > SYSTOLIC_MAX
                            || diastolic < DIASTOLIC_MIN || diastolic > DIASTOLIC_MAX) {
                        log.warn("血压异常: {}/{}", systolic, diastolic);
                        return true;
                    }
                }
            } catch (NumberFormatException e) {
                log.warn("血压格式解析失败: {}", dto.getBloodPressure());
            }
        }

        // 检查血糖
        if (dto.getBloodSugar() != null) {
            if (dto.getBloodSugar() < BLOOD_SUGAR_MIN || dto.getBloodSugar() > BLOOD_SUGAR_MAX) {
                log.warn("血糖异常: {}", dto.getBloodSugar());
                return true;
            }
        }

        // 检查心率
        if (dto.getHeartRate() != null) {
            if (dto.getHeartRate() < HEART_RATE_MIN || dto.getHeartRate() > HEART_RATE_MAX) {
                log.warn("心率异常: {}", dto.getHeartRate());
                return true;
            }
        }

        // 检查体重
        if (dto.getWeight() != null) {
            if (dto.getWeight() < WEIGHT_MIN || dto.getWeight() > WEIGHT_MAX) {
                log.warn("体重异常: {}", dto.getWeight());
                return true;
            }
        }

        return false;
    }

    /**
     * 将实体转换为视图对象
     *
     * @param health 健康记录实体
     * @return 健康记录VO
     */
    private HealthVO toVO(Health health) {
        HealthVO vo = new HealthVO();
        vo.setId(health.getId());
        vo.setElderId(health.getElderId());
        vo.setBloodPressure(health.getBloodPressure());
        vo.setBloodSugar(health.getBloodSugar());
        vo.setHeartRate(health.getHeartRate());
        vo.setWeight(health.getWeight());
        vo.setWarningFlag(health.getWarningFlag());
        vo.setRecordTime(health.getRecordTime());
        return vo;
    }

    /**
     * 将健康记录实体转换为预警视图对象，自动分析异常指标
     *
     * @param health 健康记录实体
     * @return 预警VO
     */
    private AlertVO toAlertVO(Health health) {
        AlertVO vo = new AlertVO();
        vo.setId(health.getId());
        vo.setElderId(health.getElderId());
        vo.setWarningTime(health.getRecordTime());
        vo.setIsRead(health.getIsRead() != null && health.getIsRead() == 1);

        List<String> abnormalTypes = new ArrayList<>();
        List<String> abnormalDetails = new ArrayList<>();

        if (health.getBloodPressure() != null && !health.getBloodPressure().isEmpty()) {
            try {
                String[] parts = health.getBloodPressure().split("/");
                if (parts.length == 2) {
                    int systolic = Integer.parseInt(parts[0].trim());
                    int diastolic = Integer.parseInt(parts[1].trim());
                    if (systolic < SYSTOLIC_MIN || systolic > SYSTOLIC_MAX
                            || diastolic < DIASTOLIC_MIN || diastolic > DIASTOLIC_MAX) {
                        abnormalTypes.add("血压异常");
                        abnormalDetails.add("血压 " + health.getBloodPressure() + "，正常范围 "
                                + SYSTOLIC_MIN + "/" + DIASTOLIC_MIN + "~"
                                + SYSTOLIC_MAX + "/" + DIASTOLIC_MAX);
                    }
                }
            } catch (NumberFormatException e) {
                log.warn("血压格式解析失败: {}", health.getBloodPressure());
            }
        }

        if (health.getBloodSugar() != null) {
            if (health.getBloodSugar() < BLOOD_SUGAR_MIN || health.getBloodSugar() > BLOOD_SUGAR_MAX) {
                abnormalTypes.add("血糖异常");
                abnormalDetails.add("血糖 " + health.getBloodSugar() + " mmol/L，正常范围 "
                        + BLOOD_SUGAR_MIN + "~" + BLOOD_SUGAR_MAX);
            }
        }

        if (health.getHeartRate() != null) {
            if (health.getHeartRate() < HEART_RATE_MIN || health.getHeartRate() > HEART_RATE_MAX) {
                abnormalTypes.add("心率异常");
                abnormalDetails.add("心率 " + health.getHeartRate() + " bpm，正常范围 "
                        + HEART_RATE_MIN + "~" + HEART_RATE_MAX);
            }
        }

        if (health.getWeight() != null) {
            if (health.getWeight() < WEIGHT_MIN || health.getWeight() > WEIGHT_MAX) {
                abnormalTypes.add("体重异常");
                abnormalDetails.add("体重 " + health.getWeight() + " kg，正常范围 "
                        + WEIGHT_MIN + "~" + WEIGHT_MAX);
            }
        }

        vo.setAbnormalType(abnormalTypes.isEmpty() ? "指标异常" : String.join("、", abnormalTypes));
        vo.setAbnormalDetail(abnormalDetails.isEmpty() ? "部分健康指标超出正常范围" : String.join("；", abnormalDetails));
        return vo;
    }
}

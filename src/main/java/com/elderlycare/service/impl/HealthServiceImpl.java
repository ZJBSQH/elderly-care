package com.elderlycare.service.impl;

import com.elderlycare.common.util.HealthCheckUtil;
import com.elderlycare.mapper.HealthMapper;
import com.elderlycare.common.Result;
import com.elderlycare.common.exception.BusinessException;
import com.elderlycare.pojo.dto.health.HealthDTO;
import com.elderlycare.pojo.entity.Health;
import com.elderlycare.pojo.vo.HealthTrendVO;
import com.elderlycare.pojo.vo.HealthStatisticsVO;
import com.elderlycare.pojo.vo.HealthVO;
import com.elderlycare.service.HealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

import static com.elderlycare.common.exception.ErrorCode.*;

/**
 * 健康数据服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthServiceImpl implements HealthService {
    
    private final HealthMapper healthMapper;
    
    @Override
    public Result<HealthVO> saveHealth(HealthDTO request) {
        // 1. 创建健康记录
        Health health = new Health();
        BeanUtils.copyProperties(request, health);
        health.setRecordTime(LocalDateTime.now());
        health.setWarningFlag(checkWarning(
                request.getBloodPressure(), request.getBloodSugar(),
                request.getHeartRate(), request.getWeight()));

        // 3. 保存到数据库
        healthMapper.insert(health);
        log.info("健康数据录入成功，elderId: {}, warningFlag: {}",
                 request.getElderId(), health.getWarningFlag());

        return Result.success(convertToVO(health));
    }
    
    @Override
    public Result<List<HealthVO>> getTodayRecords(Integer elderId) {
        List<Health> records = healthMapper.selectToday(elderId);
        List<HealthVO> voList = convertToVO(records);
        return Result.success(voList);
    }

    @Override
    public Result<List<HealthVO>> getHistoryRecords(
            Integer elderId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        List<Health> records = healthMapper.selectByDateRange(elderId, startDate, endDate);
        List<HealthVO> voList = convertToVO(records);
        return Result.success(voList);
    }
    
    @Override
    public Result<HealthVO> getLatestRecord(Integer elderId) {
        log.info("查询老人最新健康记录，elderId: {}", elderId);
        Health health = healthMapper.selectLatest(elderId);
        if (health == null) {
            log.warn("老人{}暂无健康记录", elderId);
            return Result.success(null);
        }
        log.info("查询成功，记录ID: {}", health.getId());
        return Result.success(convertToVO(health));
    }

    // 健康趋势
    @Override
    public Result<HealthTrendVO> getHealthTrend(Integer elderId, LocalDateTime startDate, LocalDateTime endDate) {
        // 1. 查询健康记录
        List<Health> records = healthMapper.selectByDateRange(elderId, startDate, endDate);

        if (records == null || records.isEmpty()) {
            throw new BusinessException("暂无健康数据");
        }

        // 2. 提取各项指标数据
        List<String> dates = new ArrayList<>();
        List<Integer> systolicData = new ArrayList<>();
        List<Integer> diastolicData = new ArrayList<>();
        List<BigDecimal> bloodSugarData = new ArrayList<>();
        List<Integer> heartRateData = new ArrayList<>();
        List<BigDecimal> weightData = new ArrayList<>();
        List<Integer> warningFlags = new ArrayList<>();

        for (Health health : records) {
            // 日期格式化
            dates.add(health.getRecordTime().toLocalDate().toString());

            int[] bp = HealthCheckUtil.parseBloodPressure(health.getBloodPressure());
            if (bp != null) {
                systolicData.add(bp[0]);
                diastolicData.add(bp[1]);
            } else {
                systolicData.add(null);
                diastolicData.add(null);
            }

            // 其他指标
            bloodSugarData.add(health.getBloodSugar());
            heartRateData.add(health.getHeartRate());
            weightData.add(health.getWeight());
            warningFlags.add(health.getWarningFlag());
        }

        // 3. 构建返回对象
        HealthTrendVO trendVO = new HealthTrendVO();
        trendVO.setDates(dates);

        // 血压趋势
        HealthTrendVO.BloodPressureTrend bpTrend =
                new HealthTrendVO.BloodPressureTrend(systolicData, diastolicData, warningFlags);
        trendVO.setBloodPressure(bpTrend);

        // 血糖趋势
        HealthTrendVO.MetricTrend sugarTrend =
                new HealthTrendVO.MetricTrend(bloodSugarData, warningFlags);
        trendVO.setBloodSugar(sugarTrend);

        // 心率趋势
        HealthTrendVO.MetricTrend heartTrend =
                new HealthTrendVO.MetricTrend(heartRateData, warningFlags);
        trendVO.setHeartRate(heartTrend);

        // 体重趋势
        HealthTrendVO.MetricTrend weightTrend =
                new HealthTrendVO.MetricTrend(weightData, warningFlags);
        trendVO.setWeight(weightTrend);

        return Result.success(trendVO);
    }
    private int checkWarning(String bloodPressure, BigDecimal bloodSugar,
                            Integer heartRate, BigDecimal weight) {
        return HealthCheckUtil.hasAnyAbnormal(bloodPressure, bloodSugar, heartRate, weight) ? 1 : 0;
    }
    
    /**
     * 转换为 VO
     */
    private List<HealthVO> convertToVO(List<Health> records) {
        return records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为 VO
     */
    private static void setIntStats(HealthStatisticsVO s, IntSummaryStatistics stats,
                                     java.util.function.BiConsumer<HealthStatisticsVO, Integer> avgSetter,
                                     java.util.function.BiConsumer<HealthStatisticsVO, Integer> maxSetter,
                                     java.util.function.BiConsumer<HealthStatisticsVO, Integer> minSetter) {
        if (stats.getCount() == 0) return;
        avgSetter.accept(s, (int) stats.getAverage());
        maxSetter.accept(s, stats.getMax());
        minSetter.accept(s, stats.getMin());
    }

    /**
     * BigDecimal 累加器——边遍历边统计，避免中间 List
     */
    private static class BigDecimalStats {
        private BigDecimal sum = BigDecimal.ZERO;
        private BigDecimal max, min;
        private int count;

        void accept(BigDecimal v) {
            sum = sum.add(v);
            if (max == null || v.compareTo(max) > 0) max = v;
            if (min == null || v.compareTo(min) < 0) min = v;
            count++;
        }

        void applyTo(java.util.function.Consumer<BigDecimal> avgSetter,
                     java.util.function.Consumer<BigDecimal> maxSetter,
                     java.util.function.Consumer<BigDecimal> minSetter) {
            if (count == 0) return;
            avgSetter.accept(sum.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP));
            maxSetter.accept(max);
            minSetter.accept(min);
        }
    }

    private HealthVO convertToVO(Health health) {
        HealthVO vo = new HealthVO();
        BeanUtils.copyProperties(health, vo);
        return vo;
    }

    // 删除健康记录
    @Override
    public Result<Void> deleteRecord(Integer id) {
        if (id == null) {
            throw new BusinessException("记录 ID 不能为空");
        }

        int rows = healthMapper.deleteById(id);
        if (rows > 0) {
            log.info("删除健康记录成功，id: {}", id);
            return Result.success(null);
        } else {
            throw new BusinessException("删除失败，记录不存在");
        }
    }

    /**
     * 批量删除健康记录
     */
    @Override
    public Result<Void> deleteRecordsByDateRange(Integer elderId, LocalDateTime startDate, LocalDateTime endDate) {
        if (elderId == null) {
            throw new BusinessException("老人 ID 不能为空");
        }
        if (startDate == null || endDate == null) {
            throw new BusinessException("日期范围不能为空");
        }

        int rows = healthMapper.deleteByDateRange(elderId, startDate, endDate);
        log.info("批量删除健康记录成功，elderId: {}, 删除数量：{}", elderId, rows);
        return Result.success(null);
    }

    /**
     * 更新健康记录
     */
    @Override
    public Result<HealthVO> updateRecord(Integer id, HealthDTO request) {
        if (id == null) {
            throw new BusinessException(PARAM_ERROR, "记录 ID 不能为空");
        }

        // 1. 查询原记录
        Health health = healthMapper.selectById(id);
        if (health == null) {
            throw new BusinessException("记录不存在");
        }

        // 2. 更新数据
        health.setBloodPressure(request.getBloodPressure());
        health.setBloodSugar(request.getBloodSugar());
        health.setHeartRate(request.getHeartRate());
        health.setWeight(request.getWeight());

        // 3. 重新判断是否异常
        int warningFlag = checkWarning(
            request.getBloodPressure(),
            request.getBloodSugar(),
            request.getHeartRate(),
            request.getWeight()
        );
        health.setWarningFlag(warningFlag);

        // 4. 保存到数据库
        healthMapper.updateById(health);
        log.info("更新健康记录成功，id: {}, warningFlag: {}", id, warningFlag);

        return Result.success(convertToVO(health));
    }

    /**
     * 获取健康数据统计
     */
    @Override
    public Result<HealthStatisticsVO> getStatistics(Integer elderId, LocalDateTime startDate, LocalDateTime endDate) {
        if (elderId == null) throw new BusinessException(PARAM_ERROR, "老人 ID 不能为空");
        if (startDate == null || endDate == null) throw new BusinessException(PARAM_ERROR, "日期范围不能为空");

        List<Health> records = healthMapper.selectByDateRange(elderId, startDate, endDate);
        if (records == null || records.isEmpty()) throw new BusinessException("暂无健康数据");

        // 一次遍历完成所有统计，无需中间 List
        IntSummaryStatistics systolicStats = new IntSummaryStatistics();
        IntSummaryStatistics diastolicStats = new IntSummaryStatistics();
        IntSummaryStatistics heartRateStats = new IntSummaryStatistics();
        BigDecimalStats sugarStats = new BigDecimalStats();
        BigDecimalStats weightStats = new BigDecimalStats();
        int totalRecords = records.size();
        int abnormalRecords = 0;

        for (Health h : records) {
            if (h.getWarningFlag() == 1) abnormalRecords++;

            int[] bp = HealthCheckUtil.parseBloodPressure(h.getBloodPressure());
            if (bp != null) {
                systolicStats.accept(bp[0]);
                diastolicStats.accept(bp[1]);
            }
            if (h.getHeartRate() != null) heartRateStats.accept(h.getHeartRate());
            if (h.getBloodSugar() != null) sugarStats.accept(h.getBloodSugar());
            if (h.getWeight() != null) weightStats.accept(h.getWeight());
        }

        HealthStatisticsVO s = new HealthStatisticsVO();
        s.setElderId(elderId);
        s.setStartDate(startDate);
        s.setEndDate(endDate);
        s.setTotalRecords(totalRecords);
        s.setAbnormalRecords(abnormalRecords);
        s.setNormalRecords(totalRecords - abnormalRecords);
        s.setAbnormalRate(BigDecimal.valueOf(abnormalRecords).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalRecords), 1, BigDecimal.ROUND_HALF_UP));

        setIntStats(s, systolicStats, HealthStatisticsVO::setAvgSystolic,
                HealthStatisticsVO::setMaxSystolic, HealthStatisticsVO::setMinSystolic);
        setIntStats(s, diastolicStats, HealthStatisticsVO::setAvgDiastolic,
                HealthStatisticsVO::setMaxDiastolic, HealthStatisticsVO::setMinDiastolic);
        setIntStats(s, heartRateStats, HealthStatisticsVO::setAvgHeartRate,
                HealthStatisticsVO::setMaxHeartRate, HealthStatisticsVO::setMinHeartRate);

        sugarStats.applyTo(s::setAvgBloodSugar, s::setMaxBloodSugar, s::setMinBloodSugar);
        weightStats.applyTo(s::setAvgWeight, s::setMaxWeight, s::setMinWeight);

        log.info("获取健康数据统计成功，elderId: {}, 总记录数：{}", elderId, totalRecords);
        return Result.success(s);
    }
}

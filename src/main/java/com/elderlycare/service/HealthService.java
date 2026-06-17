package com.elderlycare.service;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.dto.health.HealthDTO;
import com.elderlycare.pojo.vo.HealthStatisticsVO;
import com.elderlycare.pojo.vo.HealthTrendVO;
import com.elderlycare.pojo.vo.HealthVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康数据服务接口
 */
public interface HealthService {

    /**
     * 录入健康数据
     */
    Result<HealthVO> saveHealth(HealthDTO request);

    /**
     * 查询今日记录
     */
    Result<List<HealthVO>> getTodayRecords(Integer elderId);

    /**
     * 查询历史记录（按日期范围）
     */
    Result<List<HealthVO>> getHistoryRecords(
            Integer elderId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * 查询最新记录
     */
    Result<HealthVO> getLatestRecord(Integer elderId);

    /**
     * 获取健康数据趋势
     */
    Result<HealthTrendVO> getHealthTrend(Integer elderId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 删除单条记录
     */
    Result<Void> deleteRecord(Integer id);

    /**
     * 批量删除记录（按日期范围）
     */
    Result<Void> deleteRecordsByDateRange(
            Integer elderId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * 更新健康记录
     */
    Result<HealthVO> updateRecord(Integer id, HealthDTO request);

    /**
     * 获取健康数据统计报告
     */
    Result<HealthStatisticsVO> getStatistics(
            Integer elderId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

}

package com.elderlycare.health.service;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.health.dto.HealthDTO;
import com.elderlycare.health.dto.HealthQuery;
import com.elderlycare.health.vo.AlertVO;
import com.elderlycare.health.vo.HealthVO;

import java.util.List;
import java.util.Map;

/**
 * 健康记录服务接口
 *
 * @author 郑
 */
public interface HealthService {

    /**
     * 保存健康记录
     *
     * @param healthDTO 健康记录DTO
     * @return 保存结果
     */
    Result<HealthVO> saveHealth(HealthDTO healthDTO);

    /**
     * 获取当天健康记录
     *
     * @param elderId 老人ID
     * @return 当天健康记录列表
     */
    Result<List<HealthVO>> getTodayRecords(Integer elderId);

    /**
     * 获取历史健康记录（分页/不分页由 query.pageNum 控制，传 0 不分页）
     *
     * @param query 查询参数
     * @return 健康记录列表
     */
    Result<List<HealthVO>> getHistoryRecords(HealthQuery query);

    /**
     * 获取最新一条健康记录
     *
     * @param elderId 老人ID
     * @return 最新健康记录
     */
    Result<HealthVO> getLatestRecord(Integer elderId);

    /**
     * 获取健康统计信息
     *
     * @param elderId 老人ID
     * @return 统计数据（总记录数、异常记录数、各项平均值）
     */
    Result<Map<String, Object>> getStatistics(Integer elderId);

    /**
     * 删除健康记录
     *
     * @param id 记录ID
     * @return 删除结果
     */
    Result<Void> deleteRecord(Long id);

    /**
     * 更新健康记录
     *
     * @param id        记录ID
     * @param healthDTO 更新的数据
     * @return 更新后的记录
     */
    Result<HealthVO> updateRecord(Long id, HealthDTO healthDTO);

    /**
     * 获取预警列表
     *
     * @param elderId 老人ID
     * @return 预警列表
     */
    Result<List<AlertVO>> getAlertList(Integer elderId);

    /**
     * 获取未读预警数量
     *
     * @param elderId 老人ID
     * @return 未读预警数量
     */
    Result<Integer> getUnreadAlertCount(Integer elderId);

    /**
     * 标记预警已读
     *
     * @param id 健康记录ID
     * @return 操作结果
     */
    Result<Void> markAlertRead(Long id);

    /**
     * 标记全部预警已读
     *
     * @param elderId 老人ID
     * @return 操作结果
     */
    Result<Void> markAllAlertsRead(Integer elderId);
}

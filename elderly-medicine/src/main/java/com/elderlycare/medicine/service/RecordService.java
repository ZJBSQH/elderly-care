package com.elderlycare.medicine.service;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.medicine.dto.RecordQuery;
import com.elderlycare.medicine.vo.RecordVO;

import java.util.List;

/**
 * 服药记录服务接口
 *
 * @author 郑
 */
public interface RecordService {

    /**
     * 服药确认
     *
     * @param taskId  用药计划 ID
     * @param elderId 老人 ID
     * @return 操作结果
     */
    Result<Void> takeMedicine(Integer taskId, Integer elderId);

    /**
     * 获取今日服药记录
     *
     * @param elderId 老人 ID
     * @return 今日服药记录列表
     */
    Result<List<RecordVO>> getTodayRecords(Integer elderId);

    /**
     * 获取历史服药记录（支持分页 + 日期/状态筛选）
     */
    Result<List<RecordVO>> getHistoryRecords(RecordQuery query);

    /**
     * 检查今日是否已服药
     *
     * @param taskId 用药计划 ID
     * @return { status: 1-已服, 0-未服 }
     */
    Result<java.util.Map<String, Integer>> checkRecord(Integer taskId);

    /**
     * 标记漏服
     *
     * @param taskId  用药计划 ID
     * @param elderId 老人 ID
     * @return 操作结果
     */
    Result<Void> markMissed(Integer taskId, Integer elderId);
}

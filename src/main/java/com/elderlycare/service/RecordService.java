package com.elderlycare.service;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.dto.record.RecordHistoryQueryRequest;
import com.elderlycare.pojo.vo.MedicineExportVO;
import com.elderlycare.pojo.vo.RecordVO;

import java.util.List;
import java.util.Map;

/**
 * 服药记录服务接口
 */
public interface RecordService {

    /**
     * 记录服药
     */
    Result<RecordVO> takeMedicine(Integer planId);

    /**
     * 查询今日记录
     */
    Result<List<RecordVO>> getTodayRecords(Integer elderId);

    /**
     * 检查是否已服药
     */
    Result<Map<String, Object>> checkRecord(Integer taskId);

    /**
     * 标记为漏服
     */
    Result<RecordVO> markAsMissed(Integer taskId);

    /**
     * 获取记录历史
     */
    Result<List<RecordVO>> getHistoryRecords(RecordHistoryQueryRequest request);

    /**
     * 导出用药清单
     */
    Result<List<MedicineExportVO>> exportMedicineList(RecordHistoryQueryRequest request);

    /**
     * 家属查看绑定的老人列表
     */
    Result<List<Map<String, Object>>> getBoundEldersForFamily();

    /**
     * 获取今日记录（需授权）
     */
    Result<List<RecordVO>> getTodayRecordsWithAuth(Integer elderId);

}

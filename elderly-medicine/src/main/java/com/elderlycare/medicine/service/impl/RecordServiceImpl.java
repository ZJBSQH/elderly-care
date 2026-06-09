package com.elderlycare.medicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.core.result.Result;
import com.elderlycare.common.core.util.BeanUtil;
import com.elderlycare.medicine.dto.RecordQuery;
import com.elderlycare.medicine.entity.Medicine;
import com.elderlycare.medicine.entity.Record;
import com.elderlycare.medicine.mapper.MedicineMapper;
import com.elderlycare.medicine.mapper.RecordMapper;
import com.elderlycare.medicine.service.RecordService;
import com.elderlycare.medicine.vo.RecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服药记录服务实现
 *
 * @author 郑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final RecordMapper recordMapper;
    private final MedicineMapper medicineMapper;

    /** 服药确认 */
    @Override
    public Result<Void> takeMedicine(Integer taskId, Integer elderId) {
        Record record = new Record();
        record.setTaskId(taskId);
        record.setElderId(elderId);
        record.setRemindDate(LocalDate.now());
        record.setRecordTime(LocalDateTime.now());
        record.setStatus(1);
        recordMapper.insert(record);
        log.info("服药确认成功，taskId: {}, elderId: {}", taskId, elderId);
        return Result.success();
    }

    /** 获取今日服药记录 */
    @Override
    public Result<List<RecordVO>> getTodayRecords(Integer elderId) {
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getElderId, elderId)
                .eq(Record::getRemindDate, LocalDate.now());
        List<Record> recordList = recordMapper.selectList(wrapper);
        List<RecordVO> voList = recordList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /** 获取历史服药记录（支持分页 + 日期/状态筛选） */
    @Override
    public Result<List<RecordVO>> getHistoryRecords(RecordQuery query) {
        Page<Record> pageParam = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getElderId, query.getElderId());
        if (query.getStartDate() != null) wrapper.ge(Record::getRemindDate, query.getStartDate());
        if (query.getEndDate() != null) wrapper.le(Record::getRemindDate, query.getEndDate());
        if (query.getStatus() != null) wrapper.eq(Record::getStatus, query.getStatus());
        wrapper.orderByDesc(Record::getRemindDate);
        Page<Record> recordPage = recordMapper.selectPage(pageParam, wrapper);
        List<RecordVO> voList = recordPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /** 检查今日是否已服药 */
    @Override
    public Result<Map<String, Integer>> checkRecord(Integer taskId) {
        // 查询今日该任务的最新记录（包含已服和漏服）
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getTaskId, taskId)
                .eq(Record::getRemindDate, LocalDate.now())
                .orderByDesc(Record::getId)
                .last("LIMIT 1");
        Record record = recordMapper.selectOne(wrapper);
        Map<String, Integer> result = new HashMap<>();
        result.put("status", record != null ? record.getStatus() : 0);
        return Result.success(result);
    }

    /** 标记漏服 */
    @Override
    public Result<Void> markMissed(Integer taskId, Integer elderId) {
        Record record = new Record();
        record.setTaskId(taskId);
        record.setElderId(elderId);
        record.setRemindDate(LocalDate.now());
        record.setRecordTime(LocalDateTime.now());
        record.setStatus(2);
        record.setRemark("漏服");
        recordMapper.insert(record);
        log.info("漏服记录已标记，taskId: {}, elderId: {}", taskId, elderId);
        return Result.success();
    }

    /** 服药状态文本数组 */
    private static final String[] STATUS_TEXT = {"待服", "已服", "漏服", "跳过"};

    /**
     * 将 Record 实体转换为 RecordVO
     *
     * @param record 服药记录实体
     * @return 服药记录视图对象
     */

    private RecordVO convertToVO(Record record) {
        RecordVO vo = new RecordVO();
        BeanUtil.copyProperties(record, vo);
        // 状态文本：业务计算
        int s = record.getStatus() != null ? record.getStatus() : 0;
        vo.setStatusText(s >= 0 && s < STATUS_TEXT.length ? STATUS_TEXT[s] : "");
        // 关联药品名称：跨表查询
        Medicine medicine = medicineMapper.selectById(record.getTaskId());
        if (medicine != null) {
            vo.setMedicineName(medicine.getMedicineName());
            vo.setDosage(medicine.getDosage());
            vo.setFrequency(medicine.getFrequency());
        }
        return vo;
    }
}

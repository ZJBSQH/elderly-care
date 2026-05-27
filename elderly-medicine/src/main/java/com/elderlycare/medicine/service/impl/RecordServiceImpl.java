package com.elderlycare.medicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.core.result.Result;
import com.elderlycare.medicine.entity.Record;
import com.elderlycare.medicine.mapper.RecordMapper;
import com.elderlycare.medicine.service.RecordService;
import com.elderlycare.medicine.vo.RecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    @Override
    public Result<List<RecordVO>> getHistoryRecords(Integer elderId, Integer page, Integer size) {
        Page<Record> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getElderId, elderId)
                .orderByDesc(Record::getRemindDate);
        Page<Record> recordPage = recordMapper.selectPage(pageParam, wrapper);
        List<RecordVO> voList = recordPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    public Result<Boolean> checkRecord(Integer taskId) {
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getTaskId, taskId)
                .eq(Record::getRemindDate, LocalDate.now())
                .eq(Record::getStatus, 1);
        boolean exists = recordMapper.selectCount(wrapper) > 0;
        return Result.success(exists);
    }

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

    /**
     * 将 Record 实体转换为 RecordVO
     *
     * @param record 服药记录实体
     * @return 服药记录视图对象
     */
    private RecordVO convertToVO(Record record) {
        RecordVO vo = new RecordVO();
        vo.setId(record.getId());
        vo.setTaskId(record.getTaskId());
        vo.setElderId(record.getElderId());
        vo.setRemindDate(record.getRemindDate());
        vo.setRecordTime(record.getRecordTime());
        vo.setStatus(record.getStatus());
        vo.setRemark(record.getRemark());
        return vo;
    }
}

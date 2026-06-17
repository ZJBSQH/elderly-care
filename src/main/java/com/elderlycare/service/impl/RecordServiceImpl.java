package com.elderlycare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.common.util.SecurityUtil;
import com.elderlycare.mapper.*;
import com.elderlycare.common.Result;
import com.elderlycare.common.exception.BusinessException;
import com.elderlycare.pojo.dto.record.RecordHistoryQueryRequest;
import com.elderlycare.pojo.entity.*;
import com.elderlycare.pojo.entity.Record;
import com.elderlycare.pojo.vo.MedicineExportVO;
import com.elderlycare.pojo.vo.RecordVO;
import com.elderlycare.service.RecordService;
import com.elderlycare.common.websocket.NotifyWebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.elderlycare.common.exception.ErrorCode.*;

/**
 * 服药记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final RecordMapper recordMapper;
    private final RemindTaskMapper remindTaskMapper;
    private final MedicineMapper medicineMapper;
    private final FamilyMapper familyMapper;
    private final UserMapper userMapper;
    private final ElderMapper elderMapper;
    private final SecurityUtil securityUtil;

    /**
     * 添加服药记录推送给家属
     */
    @Override
    public Result<RecordVO> takeMedicine(Integer taskId) {
        RemindTask task = checkTaskExists(taskId);
        Record existing = recordMapper.selectByTaskId(taskId);
        if (existing != null && existing.getStatus() == 1) {
            throw new BusinessException("已记录服药");
        }
        return createRecord(task, 1, "已服药", "服药完成");
    }

    @Override
    public Result<List<RecordVO>> getTodayRecords(Integer elderId) {
        List<Record> records = recordMapper.selectToday(elderId);
        return Result.success(records.stream().map(this::toRecordVO).toList());
    }

    @Override
    public Result<Map<String, Object>> checkRecord(Integer taskId) {
        Map<String, Object> result = new HashMap<>();

        Record record = recordMapper.selectByTaskId(taskId);

        if (record == null) {
            result.put("isTaken", false);
            result.put("status", 0);
        } else {
            result.put("isTaken", record.getStatus() == 1);
            result.put("status", record.getStatus());
            result.put("recordTime", record.getRecordTime());
        }

        return Result.success(result);
    }

    /**
     * 标记为漏服并推送家属
     */
    @Override
    public Result<RecordVO> markAsMissed(Integer taskId) {
        RemindTask task = checkTaskExists(taskId);
        Record existing = recordMapper.selectByTaskId(taskId);
        if (existing != null) {
            throw new BusinessException("已记录，无需标记");
        }
        return createRecord(task, 0, "超时未服", "漏服提醒");
    }

    /**
     * 获取记录历史（支持分页）
     */
    @Override
    public Result<List<RecordVO>> getHistoryRecords(RecordHistoryQueryRequest request) {
        List<Record> records = recordMapper.selectHistory(
                request.getElderId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStatus(),
                request.getTaskId()
        );

        // 手动分页处理
        int pageNum = request.getPageNum() != null ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 20;
        int total = records.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<RecordVO> pagedRecords = fromIndex < total ? records.subList(fromIndex, toIndex).stream().map(this::toRecordVO).toList() : new ArrayList<>();

        return Result.success(pagedRecords);
    }

    /**
     * 导出服药列表
     */
    @Override
    public Result<List<MedicineExportVO>> exportMedicineList(RecordHistoryQueryRequest request) {
        List<Record> records = recordMapper.selectHistory(
                request.getElderId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStatus(),
                request.getTaskId()
        );

        // 批量查询药品，避免 N+1
        List<Integer> medicineIds = records.stream()
                .map(Record::getTaskId).distinct().toList();
        Map<Integer, Medicine> medicineMap = medicineMapper.selectBatchIds(medicineIds).stream()
                .collect(Collectors.toMap(Medicine::getId, m -> m));

        List<MedicineExportVO> exportList = records.stream().map(record -> {
            MedicineExportVO vo = new MedicineExportVO();
            vo.setId(record.getId());
            vo.setRemindDate(record.getRemindDate());
            vo.setRecordTime(record.getRecordTime());
            vo.setStatus(record.getStatus());
            vo.setStatusText(record.getStatus() == 1 ? "已服用" : "未服用");
            vo.setRemark(record.getRemark() != null ? record.getRemark() : "");

            Medicine medicine = medicineMap.get(record.getTaskId());
            if (medicine != null) {
                vo.setMedicineName(medicine.getMedicineName());
                vo.setDosage(medicine.getDosage());
                vo.setFrequency(medicine.getFrequency());
            }

            return vo;
        }).toList();

        return Result.success(exportList);
    }

    /**
     * 家属查看绑定的老人列表
     */
    @Override
    public Result<List<Map<String, Object>>> getBoundEldersForFamily() {
        Integer currentUserId = securityUtil.getCurrentUserId();

        List<Elder> elders = familyMapper.selectBoundEldersByFamilyUserId(currentUserId);

        if (elders.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        List<Integer> elderIds = elders.stream().map(Elder::getId).toList();
        List<Integer> userIds = elders.stream().map(Elder::getUserId).toList();

        // 批量查询用户信息
        Map<Integer, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 批量查询家属关系
        Map<Integer, Family> familyMap = familyMapper.selectList(
                new LambdaQueryWrapper<Family>()
                        .eq(Family::getFamilyUserId, currentUserId)
                        .in(Family::getElderId, elderIds)
                        .eq(Family::getBindStatus, 1)
        ).stream().collect(Collectors.toMap(Family::getElderId, f -> f));

        // 批量统计待服药任务数
        Map<Integer, Long> pendingCountMap = remindTaskMapper.selectList(
                new LambdaQueryWrapper<RemindTask>()
                        .in(RemindTask::getElderId, elderIds)
                        .eq(RemindTask::getRemindDate, LocalDate.now())
                        .eq(RemindTask::getStatus, 1)
        ).stream().collect(Collectors.groupingBy(RemindTask::getElderId, Collectors.counting()));

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Elder elder : elders) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", elder.getId());

            User elderUser = userMap.get(elder.getUserId());
            if (elderUser != null) {
                item.put("name", elderUser.getName() != null ? elderUser.getName() : "未设置姓名");
                item.put("age", elderUser.getAge());
                item.put("sex", elderUser.getSex());
                item.put("phone", elderUser.getPhone());
                item.put("avatar", elderUser.getAvatar());
            } else {
                item.put("name", "未知老人");
                item.put("age", null);
                item.put("sex", null);
                item.put("phone", null);
                item.put("avatar", null);
            }

            Family family = familyMap.get(elder.getId());
            item.put("relation", family != null && family.getRelation() != null ? family.getRelation() : "其他");

            item.put("pendingTasks", pendingCountMap.getOrDefault(elder.getId(), 0L).intValue());

            resultList.add(item);
        }

        log.info("用户 {} 查询到 {} 个绑定的老人", currentUserId, resultList.size());
        return Result.success(resultList);
    }

    /**
     * 验证权限后查看老人服药记录
     */
    @Override
    public Result<List<RecordVO>> getTodayRecordsWithAuth(Integer elderId) {
        Integer currentUserId = securityUtil.getCurrentUserId();

        // 验证权限：检查是否已绑定该老人
        boolean hasPermission = familyMapper.existsBinding(currentUserId, elderId);
        if (!hasPermission) {
            throw new BusinessException(FORBIDDEN, "无权查看该老人的信息");
        }

        // 查询今日记录
        List<Record> records = recordMapper.selectToday(elderId);
        return Result.success(records.stream().map(this::toRecordVO).toList());
    }

    private RecordVO toRecordVO(Record record) {
        RecordVO vo = new RecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    /**
     * 推送消息给所有绑定的家属
     */
    private void pushToFamilyMembers(Integer elderId, Record record, String action) {
        try {
            List<User> familyMembers = familyMapper.selectBoundFamilyMembersByElderId(elderId);

            Map<String, Object> data = new HashMap<>();
            data.put("action", action);
            data.put("elderId", elderId);
            data.put("recordId", record.getId());
            data.put("status", record.getStatus());
            data.put("statusText", getStatusText(record.getStatus()));
            data.put("recordTime", record.getRecordTime());
            data.put("remark", record.getRemark());

            for (User family : familyMembers) {
                NotifyWebSocket.NotifyMessage message = new NotifyWebSocket.NotifyMessage(
                        200,
                        "服药状态更新",
                        data
                );
                NotifyWebSocket.sendToUser(String.valueOf(family.getId()), message);
                log.info("已推送{}通知给家属 userId: {}", action, family.getId());
            }
        } catch (Exception e) {
            log.error("推送给家属失败", e);
        }
    }

    private RemindTask checkTaskExists(Integer taskId) {
        RemindTask task = remindTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("计划不存在");
        }
        return task;
    }

    private Result<RecordVO> createRecord(RemindTask task, int status, String remark, String action) {
        Record record = new Record();
        record.setTaskId(task.getId());
        record.setElderId(task.getElderId());
        record.setRemindDate(task.getRemindDate());
        record.setRecordTime(LocalDateTime.now());
        record.setStatus(status);
        record.setRemark(remark);

        recordMapper.insert(record);
        log.info("记录{}成功，taskId: {}", remark, task.getId());
        pushToFamilyMembers(task.getElderId(), record, action);
        return Result.success(toRecordVO(record));
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待服";
            case 1: return "已服";
            case 2: return "漏服";
            case 3: return "跳过";
            default: return "未知";
        }
    }
}

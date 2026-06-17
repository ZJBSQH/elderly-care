package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.pojo.entity.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 服药记录
 */
@Mapper
public interface RecordMapper extends BaseMapper<Record> {


    /**
     * 根据计划 ID 查询记录
     */
    Record selectByTaskId(@Param("taskId") Integer taskId);

    /**
     * 查询今日记录（使用 LambdaQueryWrapper）
     */
    default List<Record> selectToday(@Param("elderId") Integer elderId) {
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getElderId, elderId)
                .eq(Record::getRemindDate, LocalDate.now())
                .orderByDesc(Record::getRecordTime);
        return selectList(wrapper);
    }

    /**
     * 统计今日记录数量
     */
    default Long countToday(@Param("taskId") Integer taskId) {
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getTaskId, taskId)
                .eq(Record::getRemindDate, LocalDate.now());
        return selectCount(wrapper);
    }

    /**
     * 查询指定日期的记录
     */
    default List<Record> selectByDate(
            @Param("elderId") Integer elderId,
            @Param("date") LocalDate date) {
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getElderId, elderId)
                .eq(Record::getRemindDate, date)
                .orderByDesc(Record::getRecordTime);
        return selectList(wrapper);
    }

    /**
     * 查询时间范围内的记录
     */
    default List<Record> selectByDateRange(
            @Param("elderId") Integer elderId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate) {
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getElderId, elderId)
                .between(Record::getRemindDate, startDate, endDate)
                .orderByDesc(Record::getRecordTime);
        return selectList(wrapper);
    }

    //查询用药历史
    default List<Record> selectHistory(
            @Param("elderId") Integer elderId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") Integer status,
            @Param("taskId") Integer taskId ){
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        if (elderId != null) {
            wrapper.eq(Record::getElderId, elderId);
        }
        if (startDate != null) {
            wrapper.ge(Record::getRemindDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Record::getRemindDate, endDate);
        }
        if (status != null) {
            wrapper.eq(Record::getStatus, status);
        }
        if (taskId != null) {
            wrapper.eq(Record::getTaskId, taskId);
        }

        wrapper.orderByDesc(Record::getRecordTime);
        return selectList(wrapper);
    }
}


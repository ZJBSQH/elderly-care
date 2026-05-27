package com.elderlycare.health.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.health.entity.Health;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 健康记录 Mapper 接口
 *
 * @author 郑
 */
@Mapper
public interface HealthMapper extends BaseMapper<Health> {

    /**
     * 查询当天健康记录
     *
     * @param elderId 老人ID
     * @return 当天健康记录列表
     */
    default List<Health> selectToday(Integer elderId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        return selectList(new LambdaQueryWrapper<Health>()
                .eq(Health::getElderId, elderId)
                .between(Health::getRecordTime, startOfDay, endOfDay)
                .orderByDesc(Health::getRecordTime));
    }

    /**
     * 查询指定日期范围内的健康记录
     *
     * @param elderId 老人ID
     * @param start   开始时间
     * @param end     结束时间
     * @return 健康记录列表
     */
    default List<Health> selectByDateRange(Integer elderId, LocalDateTime start, LocalDateTime end) {
        return selectList(new LambdaQueryWrapper<Health>()
                .eq(Health::getElderId, elderId)
                .between(Health::getRecordTime, start, end)
                .orderByDesc(Health::getRecordTime));
    }

    /**
     * 查询最新一条健康记录
     *
     * @param elderId 老人ID
     * @return 最新健康记录
     */
    default Health selectLatest(Integer elderId) {
        return selectOne(new LambdaQueryWrapper<Health>()
                .eq(Health::getElderId, elderId)
                .orderByDesc(Health::getRecordTime)
                .last("LIMIT 1"));
    }

    /**
     * 统计指定时间范围内的异常记录数量
     *
     * @param elderId 老人ID
     * @param start   开始时间
     * @param end     结束时间
     * @return 异常记录数量
     */
    default Long countAbnormalRecords(Integer elderId, LocalDateTime start, LocalDateTime end) {
        return selectCount(new LambdaQueryWrapper<Health>()
                .eq(Health::getElderId, elderId)
                .eq(Health::getWarningFlag, 1)
                .between(Health::getRecordTime, start, end));
    }

    /**
     * 查询预警列表（warning_flag=1 的记录）
     *
     * @param elderId 老人ID
     * @return 预警健康记录列表
     */
    default List<Health> selectAlerts(Integer elderId) {
        return selectList(new LambdaQueryWrapper<Health>()
                .eq(Health::getElderId, elderId)
                .eq(Health::getWarningFlag, 1)
                .orderByDesc(Health::getRecordTime));
    }

    /**
     * 统计未读预警数量
     *
     * @param elderId 老人ID
     * @return 未读预警数量
     */
    default Long countUnreadAlerts(Integer elderId) {
        return selectCount(new LambdaQueryWrapper<Health>()
                .eq(Health::getElderId, elderId)
                .eq(Health::getWarningFlag, 1)
                .eq(Health::getIsRead, 0));
    }

    /**
     * 查询指定老人未读预警记录
     *
     * @param elderId 老人ID
     * @return 未读预警记录列表
     */
    default List<Health> selectUnreadAlerts(Integer elderId) {
        return selectList(new LambdaQueryWrapper<Health>()
                .eq(Health::getElderId, elderId)
                .eq(Health::getWarningFlag, 1)
                .eq(Health::getIsRead, 0));
    }
}

package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.pojo.entity.Health;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康数据 Mapper
 */
@Mapper
public interface HealthMapper extends BaseMapper<Health> {

    /**
     * 查询今日记录
     */
    default List<Health> selectToday(@Param("elderId") Integer elderId) {
        LambdaQueryWrapper<Health> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Health::getElderId, elderId)
                .ge(Health::getRecordTime, LocalDate.now().atStartOfDay())
                .lt(Health::getRecordTime, LocalDate.now().plusDays(1).atStartOfDay())
                .orderByDesc(Health::getRecordTime);
        return selectList(wrapper);
    }

    /**
     * 按日期范围查询
     */
    default List<Health> selectByDateRange(
            @Param("elderId") Integer elderId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate) {
        LambdaQueryWrapper<Health> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Health::getElderId, elderId)
                .ge(Health::getRecordTime, startDate)
                .le(Health::getRecordTime, endDate)
                .orderByDesc(Health::getRecordTime);
        return selectList(wrapper);
    }

    /**
     * 查询最新一条记录
     */
    default Health selectLatest(@Param("elderId") Integer elderId) {
        LambdaQueryWrapper<Health> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Health::getElderId, elderId)
                .orderByDesc(Health::getRecordTime)
                .last("LIMIT 1");
        return selectOne(wrapper);
    }
    
    /**
     * 根据 elderId 和日期范围删除记录
     *
     */
    default int deleteByDateRange(
            @Param("elderId") Integer elderId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate) {
        LambdaQueryWrapper<Health> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Health::getElderId, elderId)
                .ge(Health::getRecordTime, startDate)
                .le(Health::getRecordTime, endDate);
        return delete(wrapper);
    }
    
    /**
     * 统计指定日期范围内的记录数量
     */
    default long countByDateRange(
            @Param("elderId") Integer elderId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate) {
        LambdaQueryWrapper<Health> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Health::getElderId, elderId)
                .ge(Health::getRecordTime, startDate)
                .le(Health::getRecordTime, endDate);
        return selectCount(wrapper);
    }
    
    /**
     * 统计异常记录数量
     */
    default long countAbnormalRecords(
            @Param("elderId") Integer elderId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate) {
        LambdaQueryWrapper<Health> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Health::getElderId, elderId)
                .eq(Health::getWarningFlag, 1)
                .ge(Health::getRecordTime, startDate)
                .le(Health::getRecordTime, endDate);
        return selectCount(wrapper);
    }
}

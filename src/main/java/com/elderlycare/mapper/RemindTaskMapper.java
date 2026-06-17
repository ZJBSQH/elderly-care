package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.pojo.entity.RemindTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 提醒任务 Mapper
 */
@Mapper
public interface RemindTaskMapper extends BaseMapper<RemindTask> {

    List<RemindTask> selectTodayTasks(@Param("today") LocalDate  today);

    List<RemindTask> selectByElderId(@Param("elderId") Integer elderId,
                                     @Param("remindType") Integer remindType,
                                     @Param("startDate") String startDate,
                                     @Param("endDate") String endDate);
}

package com.elderlycare.remind.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.remind.entity.RemindTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 提醒任务 Mapper
 *
 * @author 郑
 */
@Mapper
public interface RemindTaskMapper extends BaseMapper<RemindTask> {

    /**
     * 查询当天需要执行的提醒任务
     *
     * @return 当天任务列表
     */
    List<RemindTask> selectTodayTasks();

    /**
     * 查询指定用户当天需要执行的提醒任务
     *
     * @param userId 用户ID
     * @return 当天任务列表
     */
    List<RemindTask> selectTodayTasksByUserId(@Param("userId") Integer userId);

    /**
     * 查询指定老人当天需要执行的提醒任务
     *
     * @param elderId 老人ID
     * @return 当天任务列表
     */
    List<RemindTask> selectTodayTasksByElderId(@Param("elderId") Integer elderId);
}

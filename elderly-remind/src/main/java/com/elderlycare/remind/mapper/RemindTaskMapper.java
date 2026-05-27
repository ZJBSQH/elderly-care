package com.elderlycare.remind.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.remind.entity.RemindTask;
import org.apache.ibatis.annotations.Mapper;

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
}

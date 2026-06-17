package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.pojo.entity.Remind;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 提醒设置 Mapper
 */
@Mapper
public interface RemindMapper extends BaseMapper<Remind> {

    Remind selectByUserId(@Param("userId") Integer userId);
}

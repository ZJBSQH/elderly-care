package com.elderlycare.remind.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.remind.entity.Remind;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提醒设置 Mapper
 *
 * @author 郑
 */
@Mapper
public interface RemindMapper extends BaseMapper<Remind> {
}

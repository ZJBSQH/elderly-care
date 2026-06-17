package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.pojo.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置 Mapper 接口
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    
}

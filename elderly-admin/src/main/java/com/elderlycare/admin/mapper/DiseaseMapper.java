package com.elderlycare.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.admin.entity.Disease;
import org.apache.ibatis.annotations.Mapper;

/**
 * 疾病字典 Mapper
 *
 * @author 郑
 */
@Mapper
public interface DiseaseMapper extends BaseMapper<Disease> {
}

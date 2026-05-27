package com.elderlycare.medicine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.medicine.entity.Record;
import org.apache.ibatis.annotations.Mapper;

/**
 * 服药记录 Mapper
 *
 * @author 郑
 */
@Mapper
public interface RecordMapper extends BaseMapper<Record> {
}

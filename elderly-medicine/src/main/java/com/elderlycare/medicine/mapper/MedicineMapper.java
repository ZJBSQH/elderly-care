package com.elderlycare.medicine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.medicine.entity.Medicine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用药计划 Mapper
 *
 * @author 郑
 */
@Mapper
public interface MedicineMapper extends BaseMapper<Medicine> {

    /**
     * 根据老人 ID 查询用药计划列表
     *
     * @param elderId 老人 ID
     * @return 用药计划列表
     */
    List<Medicine> selectByElderId(@Param("elderId") Integer elderId);
}

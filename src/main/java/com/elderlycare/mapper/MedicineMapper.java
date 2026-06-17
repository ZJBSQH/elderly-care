package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.pojo.entity.Medicine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

//用药计划mapper
@Mapper
public interface MedicineMapper extends BaseMapper<Medicine>{
    //根据老人id查看用药计划，按服用时间排列
    List<Medicine> selectByElderId(@Param("elderId") Integer elderId);
}

package com.elderlycare.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.user.entity.Family;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FamilyMapper extends BaseMapper<Family> {

    List<Family> selectByFamilyUserId(@Param("familyUserId") Integer familyUserId);

    List<Family> selectByElderId(@Param("elderId") Integer elderId);

    default boolean existsBinding(Integer familyUserId, Integer elderId) {
        LambdaQueryWrapper<Family> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Family::getFamilyUserId, familyUserId)
                .eq(Family::getElderId, elderId)
                .eq(Family::getBindStatus, 1);
        return selectCount(wrapper) > 0;
    }
}

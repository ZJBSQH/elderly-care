package com.elderlycare.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.user.entity.Family;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 家属绑定关系Mapper
 */
@Mapper
public interface FamilyMapper extends BaseMapper<Family> {

    /**
     * 根据家属用户ID查询绑定关系列表
     */
    List<Family> selectByFamilyUserId(@Param("familyUserId") Integer familyUserId);

    /**
     * 根据老人档案ID查询绑定关系列表
     */
    List<Family> selectByElderId(@Param("elderId") Integer elderId);

    /**
     * 检查家属与老人之间是否已存在有效绑定
     */
    default boolean existsBinding(Integer familyUserId, Integer elderId) {
        LambdaQueryWrapper<Family> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Family::getFamilyUserId, familyUserId)
                .eq(Family::getElderId, elderId)
                .eq(Family::getBindStatus, 1);
        return selectCount(wrapper) > 0;
    }
}

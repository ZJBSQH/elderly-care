package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.pojo.entity.Elder;
import com.elderlycare.pojo.entity.Family;
import com.elderlycare.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FamilyMapper extends BaseMapper<Family> {
    /**
     * 查询家属绑定的所有老人
     * @param familyUserId 家属用户 ID
     * @return 老人列表
     */
    List<Elder> selectBoundEldersByFamilyUserId(@Param("familyUserId") Integer familyUserId);

    /**
     * 查询老人绑定的所有家属
     * @param elderId 老人 ID
     * @return 家属用户列表
     */
    List<User> selectBoundFamilyMembersByElderId(@Param("elderId") Integer elderId);

    /**
     * 验证家属与老人的绑定关系
     * @param familyUserId 家属用户 ID
     * @param elderId 老人 ID
     * @return true-已绑定，false-未绑定
     */
    default boolean existsBinding(@Param("familyUserId") Integer familyUserId,
                                  @Param("elderId") Integer elderId) {
        LambdaQueryWrapper<Family> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Family::getFamilyUserId, familyUserId)
                .eq(Family::getElderId, elderId)
                .eq(Family::getBindStatus, 1);
        return selectCount(wrapper) > 0;
    }

}

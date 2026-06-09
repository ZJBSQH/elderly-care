package com.elderlycare.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.user.entity.Elder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 老人健康档案Mapper
 */
@Mapper
public interface ElderMapper extends BaseMapper<Elder> {

    /**
     * 根据用户ID查询老人档案
     */
    Elder selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据二维码Token查询老人档案
     */
    @Select("SELECT * FROM elder WHERE qr_code_token = #{qrCodeToken} LIMIT 1")
    Elder selectByQrCodeToken(@Param("qrCodeToken") String qrCodeToken);
}

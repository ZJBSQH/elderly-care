package com.elderlycare.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.user.entity.Elder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ElderMapper extends BaseMapper<Elder> {

    Elder selectByUserId(@Param("userId") Integer userId);

    @Select("SELECT * FROM elder WHERE qr_code_token = #{qrCodeToken} LIMIT 1")
    Elder selectByQrCodeToken(@Param("qrCodeToken") String qrCodeToken);
}

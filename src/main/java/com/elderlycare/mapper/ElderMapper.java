package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.pojo.entity.Elder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ElderMapper extends BaseMapper<Elder> {
    Elder selectByPhone(@Param("phone") String phone);
    
    /**
     * 根据用户 ID 查询老人信息
     */
    Elder selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据二维码 token 查询老人信息
     * @param qrCodeToken 二维码 token
     * @return 老人信息
     */
    @Select("SELECT * FROM elder WHERE qr_code_token = #{qrCodeToken} LIMIT 1")
    Elder selectByQrCodeToken(@Param("qrCodeToken") String qrCodeToken);

}

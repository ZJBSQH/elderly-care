package com.elderlycare.pojo.dto;

import lombok.Data;

/**
 * 老人信息返回DTO（脱敏）
 */
@Data
public class ElderInfoDTO {
    private Integer elderId;
    private String name;
    private Integer age;
    private String sex;
    private String phone; // 脱敏手机号
    private boolean hasBound; // 是否已绑定
}
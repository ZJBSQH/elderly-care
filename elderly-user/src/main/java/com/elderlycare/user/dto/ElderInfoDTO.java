package com.elderlycare.user.dto;

import lombok.Data;

/**
 * 老人信息返回DTO（脱敏）
 */
@Data
public class ElderInfoDTO {

    /** 老人档案ID */
    private Integer elderId;

    /** 老人姓名 */
    private String name;

    /** 老人年龄 */
    private Integer age;

    /** 老人性别 */
    private String sex;

    /** 老人手机号（脱敏） */
    private String phone;

    /** 是否已绑定当前家属 */
    private boolean hasBound;
}

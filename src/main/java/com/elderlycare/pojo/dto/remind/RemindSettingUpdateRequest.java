package com.elderlycare.pojo.dto.remind;

import lombok.Data;

/**
 * 提醒设置更新请求
 */
@Data
public class RemindSettingUpdateRequest {

    private Integer userId;

    private String ringtone;

    private Integer volume;

    private String repeatMode;

    private String quietTime;
}

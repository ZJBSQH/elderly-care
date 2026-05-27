package com.elderlycare.remind.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提醒设置更新请求
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemindSettingUpdateRequest {

    private Integer userId;

    private String ringtone;

    private Integer volume;

    private String repeatMode;

    private String quietTime;
}

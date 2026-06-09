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

    /** 用户 ID */
    private Integer userId;

    /** 铃声 */
    private String ringtone;

    /** 音量 */
    private Integer volume;

    /** 重复模式 */
    private String repeatMode;

    /** 免打扰时间段 */
    private String quietTime;
}

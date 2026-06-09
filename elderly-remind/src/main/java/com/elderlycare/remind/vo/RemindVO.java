package com.elderlycare.remind.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提醒设置视图对象
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemindVO {
    /** 提醒设置 ID */
    private Integer id;
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

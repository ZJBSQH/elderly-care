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
    private Integer id;
    private Integer userId;
    private String ringtone;
    private Integer volume;
    private String repeatMode;
    private String quietTime;
}

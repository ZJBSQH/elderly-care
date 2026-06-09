package com.elderlycare.remind.dto;

import com.elderlycare.common.core.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提醒服务错误码 (6000-6999)
 *
 * @author 郑
 */
@Getter
@AllArgsConstructor
public enum RemindErrorCode implements ErrorCode {

    REMIND_NOT_EXIST(6001, "提醒设置不存在"),
    TASK_NOT_EXIST(6002, "提醒任务不存在"),
    NOTIFICATION_NOT_EXIST(6003, "通知记录不存在");

    /** 错误码 */
    private final Integer code;
    /** 错误信息 */
    private final String message;
}

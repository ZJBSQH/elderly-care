package com.elderlycare.user.dto;

import com.elderlycare.common.core.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户服务错误码 (3000-3999)
 */
@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    ELDER_NOT_EXIST(3001, "老人健康档案不存在"),
    QR_CODE_INVALID(3002, "无效的二维码"),
    ELDER_ID_PARSE_ERROR(3003, "老人ID解析失败"),
    FAMILY_BIND_FAILED(3004, "绑定失败"),
    FAMILY_ALREADY_BOUND(3005, "已绑定该老人"),
    FAMILY_NOT_BOUND(3006, "未绑定该老人");

    /** 错误码 */
    private final Integer code;

    /** 错误描述 */
    private final String message;
}

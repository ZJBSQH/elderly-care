package com.elderlycare.auth.dto;

import com.elderlycare.common.core.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 认证服务错误码 (1000-1999)
 */
@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    USER_NOT_EXIST(1001, "用户不存在"),
    USER_ALREADY_EXIST(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    PHONE_NOT_REGISTERED(1004, "手机号未注册"),
    PHONE_ALREADY_REGISTERED(1005, "手机号已注册"),
    USER_TYPE_INVALID(1006, "只能选择老人 (0) 或家属 (1)"),
    NOT_FAMILY_USER(1007, "该用户不是家属类型"),
    NOT_ELDER_USER(1008, "该用户不是老人类型"),

    SMS_CODE_ERROR(1009, "验证码错误"),
    SMS_CODE_EXPIRED(1010, "验证码已过期"),
    SMS_SEND_FAILED(1011, "短信发送失败"),

    TOKEN_INVALID(1012, "Token无效"),
    TOKEN_EXPIRED(1013, "Token已过期");

    private final Integer code;
    private final String message;
}

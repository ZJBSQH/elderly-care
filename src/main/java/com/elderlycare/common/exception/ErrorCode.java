package com.elderlycare.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 错误码枚举
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "操作成功"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    USER_TYPE_INVALID(400, "只能选择老人 (0) 或家属 (1)"),
    NOT_FAMILY_USER(400, "该用户不是家属类型"),
    NOT_ELDER_USER(400, "该用户不是老人类型"),
    QR_CODE_INVALID(400, "无效的二维码"),
    ELDER_ID_PARSE_ERROR(400, "老人ID解析失败"),
    ELDER_NOT_EXIST(400, "老人健康档案不存在"),
    FAMILY_BIND_FAILED(400, "绑定失败"),

    SYSTEM_ERROR(500,  "系统错误"),

    USER_NOT_EXIST(1001, "用户不存在"),
    USER_ALREADY_EXIST(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    PHONE_NOT_REGISTERED(1004, "手机号未注册"),
    PHONE_ALREADY_REGISTERED(1005, "手机号已注册"),

    SMS_CODE_ERROR(2001, "验证码错误"),
    SMS_CODE_EXPIRED(2002, "验证码已过期"),
    SMS_SEND_FAILED(2003, "短信发送失败"),

    TOKEN_INVALID(3001, "Token无效"),
    TOKEN_EXPIRED(3002, "Token已过期"),


    MEDICINE_NOT_EXIST(5001, "药品信息不存在"),
    REMIND_NOT_EXIST(6001, "提醒信息不存在"),


    DATABASE_ERROR(9001, "数据库操作失败"),
    REDIS_ERROR(9002, "缓存操作失败"),


    ALREADY_LIKED(8001, "已点赞该资讯"),
    LIKE_NOT_EXIST(8002, "未找到点赞记录"),

    AI_SERVICE_ERROR(7001, "AI 暂时无法回答您的问题,请稍后再试。"),
    AI_REMINDER_FAILED(7002, "AI 发起用药提醒失败"),
    AI_MISSED_INTERVENTION_FAILED(7003, "AI 处理漏服干预失败");


    private final Integer code;
    private final String message;
}


package com.elderlycare.news.dto;

import com.elderlycare.common.core.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资讯服务错误码 (8000-8999)
 *
 * @author 郑
 */
@Getter
@AllArgsConstructor
public enum NewsErrorCode implements ErrorCode {

    /** 文章不存在 */
    NEWS_NOT_EXIST(8001, "文章不存在"),

    /** 已经点赞过 */
    ALREADY_LIKED(8002, "已经点赞过该文章"),

    /** 点赞记录不存在 */
    LIKE_NOT_EXIST(8003, "点赞记录不存在"),

    /** 已经收藏过 */
    ALREADY_COLLECTED(8004, "已经收藏过该文章"),

    /** 收藏记录不存在 */
    COLLECT_NOT_EXIST(8005, "收藏记录不存在");

    private final Integer code;
    private final String message;
}

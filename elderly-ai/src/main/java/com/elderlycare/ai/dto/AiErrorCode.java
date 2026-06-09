package com.elderlycare.ai.dto;

import com.elderlycare.common.core.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI服务错误码枚举
 * <p>
 * 实现 {@link ErrorCode} 接口，错误码范围 7000-7999，
 * 用于 AI 服务中异常场景的统一错误标识。
 *
 * @author 郑
 */
@Getter
@AllArgsConstructor
public enum AiErrorCode implements ErrorCode {

    /** AI服务内部异常，如模型调用失败、数据处理错误等 */
    AI_SERVICE_ERROR(7001, "AI服务异常"),

    /** 调用用药提醒服务失败，无法获取今日提醒数据 */
    AI_REMINDER_FAILED(7002, "获取用药提醒失败"),

    /** 获取漏服干预数据失败，无法判断漏服情况并生成干预建议 */
    AI_MISSED_INTERVENTION_FAILED(7003, "获取漏服干预数据失败"),

    /** 知识库文档处理失败，如切分、嵌入或存储异常 */
    RAG_INGESTION_FAILED(7004, "文档入库失败"),

    /** 向量检索失败，如 Redis Stack 连接异常或索引不存在 */
    RAG_SEARCH_FAILED(7005, "知识检索失败"),

    /** 知识库文档不存在，更新或删除时找不到目标文档 */
    RAG_DOC_NOT_FOUND(7006, "文档不存在"),

    /** 知识库文档格式不支持 */
    RAG_UNSUPPORTED_FORMAT(7007, "不支持的文档格式"),

    /** 从资讯模块同步文章到向量知识库失败 */
    RAG_SYNC_FAILED(7008, "资讯同步失败");

    /** 错误码 */
    private final Integer code;

    /** 错误信息 */
    private final String message;
}

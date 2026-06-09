package com.elderlycare.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * RAG 查询请求 DTO
 * <p>
 * 封装用户的自然语言问题及可选的检索参数覆写。
 *
 * @author 郑
 */
@Data
public class RagQueryDTO {

    /** 用户问题（必填） */
    @NotBlank(message = "问题不能为空")
    private String question;

    /** 检索返回的最大片段数（可选，默认 5） */
    private Integer maxResults;

    /** 最低相似度阈值（可选，默认 0.65） */
    private Double minScore;
}

package com.elderlycare.ai.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 知识文档视图对象（后端 → 前端）
 * <p>
 * 用于知识库文档列表展示，不包含原始正文内容（详情需单独查询）。
 *
 * @author 郑
 */
@Data
@Builder
public class KnowledgeDocVO {

    /** 文档唯一标识 */
    private String id;

    /** 文档标题 */
    private String title;

    /** 文档分类 */
    private String category;

    /** 片段数量（该文档被切分成了多少块） */
    private Integer chunkCount;

    /** 元数据 */
    private Map<String, Object> metadata;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}

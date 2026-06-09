package com.elderlycare.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 知识文档传输对象（前端 → 后端）
 * <p>
 * 用于知识库文档的上传和更新请求，包含文档标题、分类、正文和可选元数据。
 *
 * @author 郑
 */
@Data
public class KnowledgeDocumentDTO {

    /** 文档标题（必填），如 "布洛芬缓释胶囊说明书" */
    @NotBlank(message = "文档标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    /** 文档分类（必填）: MEDICINE / NURSING / SAFETY / HEALTH / FAQ */
    @NotBlank(message = "文档分类不能为空")
    private String category;

    /** 文档正文内容（必填） */
    @NotBlank(message = "文档内容不能为空")
    private String content;

    /** 扩展元数据（可选），如 {"source": "CFDA", "drugCode": "H20013193"} */
    private Map<String, Object> metadata;
}

package com.elderlycare.news.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新文章请求
 *
 * @author 郑
 */
@Data
public class NewsUpdateRequest {

    /** 文章ID */
    @NotNull(message = "文章ID不能为空")
    private Integer id;

    /** 文章标题 */
    private String title;

    /** 文章内容 */
    private String content;

    /** 文章摘要 */
    private String summary;

    /** 文章分类 */
    private String category;

    /** 封面图片地址 */
    private String coverImage;

    /** 状态 (0-下架, 1-上架) */
    private Integer status;
}

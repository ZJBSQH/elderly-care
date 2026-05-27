package com.elderlycare.news.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发布文章请求
 *
 * @author 郑
 */
@Data
public class NewsAddRequest {

    /** 文章标题 */
    @NotBlank(message = "文章标题不能为空")
    private String title;

    /** 文章内容 */
    @NotBlank(message = "文章内容不能为空")
    private String content;

    /** 文章摘要 */
    private String summary;

    /** 文章分类 */
    @NotBlank(message = "文章分类不能为空")
    private String category;

    /** 封面图片地址 */
    private String coverImage;
}

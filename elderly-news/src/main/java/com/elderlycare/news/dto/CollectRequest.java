package com.elderlycare.news.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 收藏请求
 *
 * @author 郑
 */
@Data
public class CollectRequest {

    /** 文章ID */
    @NotNull(message = "文章ID不能为空")
    private Integer newsId;
}

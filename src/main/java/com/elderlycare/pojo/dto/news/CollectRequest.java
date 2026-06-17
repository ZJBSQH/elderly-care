package com.elderlycare.pojo.dto.news;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 收藏/取消收藏请求
 */
@Data
public class CollectRequest {

    /**
     * 文章 ID
     */
    @NotNull( message = "文章id不为空")
    private Integer newsId;
}

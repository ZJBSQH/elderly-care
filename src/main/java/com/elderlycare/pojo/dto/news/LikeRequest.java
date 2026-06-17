package com.elderlycare.pojo.dto.news;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 点赞/取消点赞请求
 */
@Data
public class LikeRequest {

    /**
     * 资讯 ID
     */
    @NotNull(message = "资讯 ID 不能为空")
    private Integer newsId;
}

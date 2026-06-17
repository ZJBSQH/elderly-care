package com.elderlycare.pojo.dto.news;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资讯更新请求（统一管理用户端和管理端）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NewsUpdateRequest extends BaseNewsRequest {

    @NotNull(message = "资讯 ID 不能为空")
    private Integer id;

    /**
     * 状态：0-草稿，1-已发布，2-下架
     * （管理端专用字段）
     */
    private Integer status;
}

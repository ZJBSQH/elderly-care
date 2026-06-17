package com.elderlycare.pojo.dto.news;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 健康科普文章新增请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NewsAddRequest extends BaseNewsRequest {
    // 继承所有公共字段：title, content, summary, category, coverImage, isRecommended
}


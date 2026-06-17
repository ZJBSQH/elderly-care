package com.elderlycare.pojo.dto.news;

import com.elderlycare.pojo.dto.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资讯请求基类
 * 抽取 NewsAddRequest、AdminNewsAddDTO 等公共字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseNewsRequest extends BaseRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过 255 个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = 500, message = "摘要长度不能超过 500 个字符")
    private String summary;

    @NotBlank(message = "分类不能为空")
    private String category;

    private String coverImage;

    private Integer isRecommended = 0;
}

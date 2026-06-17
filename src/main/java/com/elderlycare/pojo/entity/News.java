package com.elderlycare.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("news")
public class News implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("summary")
    private String summary;

    @TableField("cover_image")
    private String coverImage;

    @TableField("category")
    private String category;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("collect_count")
    private Integer collectCount;

    @TableField("is_recommended")
    private Integer isRecommended;

    @TableField("creator_id")
    private Integer creatorId;

    @TableField("publish_time")
    private LocalDateTime publishTime;

    @TableField("status")
    private Integer status;
}

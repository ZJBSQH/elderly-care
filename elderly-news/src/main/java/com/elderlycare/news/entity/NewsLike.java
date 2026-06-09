package com.elderlycare.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章点赞记录实体
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("news_like")
public class NewsLike {

    /** 点赞记录ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 用户ID */
    private Integer userId;

    /** 文章ID */
    private Integer newsId;

    /** 点赞时间 */
    private LocalDateTime createTime;
}

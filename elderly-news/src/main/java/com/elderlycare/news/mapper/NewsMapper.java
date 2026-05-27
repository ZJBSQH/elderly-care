package com.elderlycare.news.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.news.entity.News;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 资讯文章 Mapper
 *
 * @author 郑
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {

    /**
     * 根据分类查询文章列表
     */
    default List<News> selectByCategory(String category) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getCategory, category)
                .eq(News::getStatus, 1)
                .orderByDesc(News::getPublishTime);
        return selectList(wrapper);
    }

    /**
     * 根据关键词模糊搜索文章
     */
    default List<News> searchByKeyword(String keyword) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(News::getTitle, keyword)
                        .or()
                        .like(News::getContent, keyword)
                        .or()
                        .like(News::getSummary, keyword))
                .eq(News::getStatus, 1)
                .orderByDesc(News::getPublishTime);
        return selectList(wrapper);
    }

    /**
     * 查询推荐文章列表
     */
    default List<News> selectRecommended() {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getIsRecommended, 1)
                .eq(News::getStatus, 1)
                .orderByDesc(News::getPublishTime);
        return selectList(wrapper);
    }

    /**
     * 查询热门文章列表（按点赞数降序）
     */
    default List<News> selectPopular() {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getStatus, 1)
                .orderByDesc(News::getLikeCount);
        return selectList(wrapper);
    }
}

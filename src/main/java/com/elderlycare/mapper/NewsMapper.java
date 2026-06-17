 package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.elderlycare.pojo.entity.News;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 健康科普 Mapper（使用 News 实体）
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {

    /**
     * 根据分类查询文章
     */
    default List<News> selectByCategory(@Param("category") String category) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getCategory, category)
                .orderByDesc(News::getPublishTime);
        return selectList(wrapper);
    }

    /**
     * 搜索文章（标题和内容）
     */
    default List<News> searchByKeyword(@Param("keyword") String keyword) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(News::getTitle, keyword)
                        .or()
                        .like(News::getContent, keyword))
                .orderByDesc(News::getPublishTime);
        return selectList(wrapper);
    }

    /**
     * 查询推荐文章
     */
    default List<News> selectRecommended() {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getIsRecommended, 1)
                .orderByDesc(News::getPublishTime);
        return selectList(wrapper);
    }

    /**
     * 查询热门文章（按阅读量）
     */
    default List<News> selectPopular(@Param("limit") Integer limit) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(News::getViewCount)
                .last("LIMIT " + limit);
        return selectList(wrapper);
    }

    /**
     * 原子增加阅读量
     */
    default int incrementViewCount(@Param("id") Integer id) {
        LambdaUpdateWrapper<News> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(News::getId, id)
               .setSql("view_count = view_count + 1");
        return update(null, wrapper);
    }

    /**
     * 原子增加点赞数
     */
    default int incrementLikeCount(@Param("id") Integer id) {
        LambdaUpdateWrapper<News> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(News::getId, id)
               .setSql("like_count = like_count + 1");
        return update(null, wrapper);
    }

    /**
     * 原子减少点赞数（不小于0）
     */
    default int decrementLikeCount(@Param("id") Integer id) {
        LambdaUpdateWrapper<News> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(News::getId, id)
               .setSql("like_count = GREATEST(like_count - 1, 0)");
        return update(null, wrapper);
    }

    /**
     * 原子增加收藏数
     */
    default int incrementCollectCount(@Param("id") Integer id) {
        LambdaUpdateWrapper<News> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(News::getId, id)
               .setSql("collect_count = collect_count + 1");
        return update(null, wrapper);
    }

    /**
     * 原子减少收藏数（不小于0）
     */
    default int decrementCollectCount(@Param("id") Integer id) {
        LambdaUpdateWrapper<News> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(News::getId, id)
               .setSql("collect_count = GREATEST(collect_count - 1, 0)");
        return update(null, wrapper);
    }
}


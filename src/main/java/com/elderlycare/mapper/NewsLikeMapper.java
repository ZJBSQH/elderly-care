package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.pojo.entity.NewsLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 资讯点赞 Mapper
 */
@Mapper
public interface NewsLikeMapper extends BaseMapper<NewsLike> {

    /**
     * 检查用户是否已点赞
     */
    default boolean exists(@Param("userId") Integer userId, @Param("newsId") Integer newsId) {
        LambdaQueryWrapper<NewsLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsLike::getUserId, userId)
               .eq(NewsLike::getNewsId, newsId);
        return selectCount(wrapper) > 0;
    }

    /**
     * 取消点赞
     */
    default int deleteByUserAndNews(@Param("userId") Integer userId, @Param("newsId") Integer newsId) {
        LambdaQueryWrapper<NewsLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsLike::getUserId, userId)
               .eq(NewsLike::getNewsId, newsId);
        return delete(wrapper);
    }
}

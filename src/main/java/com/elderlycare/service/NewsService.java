package com.elderlycare.service;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.dto.news.CollectRequest;
import com.elderlycare.pojo.dto.news.LikeRequest;
import com.elderlycare.pojo.dto.news.NewsAddRequest;
import com.elderlycare.pojo.dto.news.NewsUpdateRequest;
import com.elderlycare.pojo.vo.HealthKnowledgeCollectVO;
import com.elderlycare.pojo.vo.HealthKnowledgeSearchVO;
import com.elderlycare.pojo.vo.HealthKnowledgeVO;

import java.util.List;

/**
 * 健康科普服务接口
 */
public interface NewsService {

    /**
     * 发布科普文章
     */
    Result<Integer> publishArticle(NewsAddRequest request, Integer creatorId);

    /**
     * 更新科普文章
     */
    Result<Void> updateArticle(Integer id, NewsUpdateRequest request);

    /**
     * 删除科普文章
     */
    Result<Void> deleteArticle(Integer id);

    /**
     * 查询文章详情
     */
    Result<HealthKnowledgeVO> getArticleDetail(Integer id, Integer userId);

    /**
     * 按分类查询文章
     */
    Result<List<HealthKnowledgeVO>> getArticlesByCategory(String category, Integer userId);

    /**
     * 查询推荐文章
     */
    Result<List<HealthKnowledgeVO>> getRecommendedArticles(Integer userId);

    /**
     * 查询热门文章
     */
    Result<List<HealthKnowledgeVO>> getPopularArticles(Integer limit, Integer userId);

    /**
     * 搜索文章
     */
    Result<HealthKnowledgeSearchVO> searchArticles(String keyword, String category, Integer page, Integer size, Integer userId);

    /**
     * 点赞资讯
     */
    Result<Void> likeArticle(LikeRequest request, Integer userId);

    /**
     * 取消点赞
     */
    Result<Void> cancelLikeArticle(LikeRequest request, Integer userId);

    /**
     * 检查是否已点赞
     */
    Result<Boolean> checkIfLiked(Integer newsId, Integer userId);

    /**
     * 收藏文章
     */
    Result<Void> collectArticle(CollectRequest request, Integer userId);

    /**
     * 取消收藏
     */
    Result<Void> cancelCollectArticle(CollectRequest request, Integer userId);

    /**
     * 查询用户的收藏列表
     */
    Result<List<HealthKnowledgeCollectVO>> getUserCollects(Integer userId);

    /**
     * 检查是否已收藏
     */
    Result<Boolean> checkIfCollected(Integer newsId, Integer userId);
}

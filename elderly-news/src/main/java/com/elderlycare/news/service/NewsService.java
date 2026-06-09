package com.elderlycare.news.service;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.news.dto.*;
import com.elderlycare.news.vo.HealthKnowledgeVO;

import java.util.List;
import java.util.Map;

/**
 * 资讯文章服务接口
 *
 * @author 郑
 */
public interface NewsService {

    /**
     * 发布文章
     */
    Result<Void> publishArticle(NewsAddRequest request);

    /**
     * 更新文章
     */
    Result<Void> updateArticle(NewsUpdateRequest request);

    /**
     * 删除文章
     */
    Result<Void> deleteArticle(Integer id);

    /**
     * 获取文章详情
     */
    Result<Map<String, Object>> getArticleDetail(Integer id);

    /**
     * 根据分类查询文章列表
     */
    Result<List<HealthKnowledgeVO>> getArticlesByCategory(String category);

    /**
     * 获取推荐文章列表
     */
    Result<List<HealthKnowledgeVO>> getRecommendedArticles();

    /**
     * 获取热门文章列表
     */
    Result<List<HealthKnowledgeVO>> getPopularArticles();

    /**
     * 关键词搜索文章
     */
    Result<List<HealthKnowledgeVO>> searchArticles(String keyword);

    /**
     * 点赞文章
     */
    Result<Void> likeArticle(LikeRequest request);

    /**
     * 取消点赞
     */
    Result<Void> cancelLikeArticle(LikeRequest request);

    /**
     * 检查是否已点赞
     */
    Result<Boolean> checkIfLiked(Integer newsId);

    /**
     * 收藏文章
     */
    Result<Void> collectArticle(CollectRequest request);

    /**
     * 取消收藏
     */
    Result<Void> cancelCollectArticle(CollectRequest request);

    /**
     * 获取用户收藏列表
     */
    Result<List<HealthKnowledgeVO>> getUserCollects();

    /**
     * 检查是否已收藏
     */
    Result<Boolean> checkIfCollected(Integer newsId);

    /**
     * 获取用于 RAG 知识库同步的文章列表（含正文全文）
     * <p>
     * 仅返回已上架（status=1）的文章，正文不做截断。
     *
     * @param category 分类过滤（可选，null 或空字符串表示全部）
     * @return 文章数据列表，每个 map 包含 id、title、content、category、summary
     */
    Result<List<Map<String, Object>>> getArticlesForSync(String category);
}

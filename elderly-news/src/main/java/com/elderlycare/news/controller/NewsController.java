package com.elderlycare.news.controller;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.news.dto.CollectRequest;
import com.elderlycare.news.dto.LikeRequest;
import com.elderlycare.news.dto.NewsAddRequest;
import com.elderlycare.news.dto.NewsUpdateRequest;
import com.elderlycare.news.service.NewsService;
import com.elderlycare.news.vo.HealthKnowledgeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 健康知识资讯控制器
 *
 * @author 郑
 */
@RestController
@RequestMapping("/health-knowledge")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    /**
     * 发布文章
     */
    @PostMapping("/article")
    public Result<Void> publishArticle(@Valid @RequestBody NewsAddRequest request) {
        return newsService.publishArticle(request);
    }

    /**
     * 更新文章
     */
    @PutMapping("/article/{id}")
    public Result<Void> updateArticle(@PathVariable Integer id, @Valid @RequestBody NewsUpdateRequest request) {
        request.setId(id);
        return newsService.updateArticle(request);
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable Integer id) {
        return newsService.deleteArticle(id);
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/article/{id}")
    public Result<Map<String, Object>> getArticleDetail(@PathVariable Integer id) {
        return newsService.getArticleDetail(id);
    }

    /**
     * 根据分类查询文章列表
     */
    @GetMapping("/articles")
    public Result<List<HealthKnowledgeVO>> getArticlesByCategory(@RequestParam(required = false) String category) {
        if (category == null || category.isEmpty()) {
            return newsService.getRecommendedArticles();
        }
        return newsService.getArticlesByCategory(category);
    }

    /**
     * 获取推荐文章列表
     */
    @GetMapping("/recommended")
    public Result<List<HealthKnowledgeVO>> getRecommendedArticles() {
        return newsService.getRecommendedArticles();
    }

    /**
     * 获取热门文章列表
     */
    @GetMapping("/popular")
    public Result<List<HealthKnowledgeVO>> getPopularArticles() {
        return newsService.getPopularArticles();
    }

    /**
     * 关键词搜索文章
     */
    @GetMapping("/search")
    public Result<List<HealthKnowledgeVO>> searchArticles(@RequestParam String keyword) {
        return newsService.searchArticles(keyword);
    }

    /**
     * 点赞文章
     */
    @PostMapping("/like")
    public Result<Void> likeArticle(@Valid @RequestBody LikeRequest request) {
        return newsService.likeArticle(request);
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/like")
    public Result<Void> cancelLikeArticle(@Valid @RequestBody LikeRequest request) {
        return newsService.cancelLikeArticle(request);
    }

    /**
     * 检查是否已点赞
     */
    @GetMapping("/like/check")
    public Result<Boolean> checkIfLiked(@RequestParam Integer newsId) {
        return newsService.checkIfLiked(newsId);
    }

    /**
     * 收藏文章
     */
    @PostMapping("/collect")
    public Result<Void> collectArticle(@Valid @RequestBody CollectRequest request) {
        return newsService.collectArticle(request);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/collect")
    public Result<Void> cancelCollectArticle(@Valid @RequestBody CollectRequest request) {
        return newsService.cancelCollectArticle(request);
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping("/collects")
    public Result<List<HealthKnowledgeVO>> getUserCollects() {
        return newsService.getUserCollects();
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/collect/check")
    public Result<Boolean> checkIfCollected(@RequestParam Integer newsId) {
        return newsService.checkIfCollected(newsId);
    }
}

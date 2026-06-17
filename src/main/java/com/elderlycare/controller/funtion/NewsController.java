package com.elderlycare.controller.funtion;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.dto.news.CollectRequest;
import com.elderlycare.pojo.dto.news.LikeRequest;
import com.elderlycare.pojo.dto.news.NewsAddRequest;
import com.elderlycare.pojo.dto.news.NewsUpdateRequest;
import com.elderlycare.pojo.vo.HealthKnowledgeCollectVO;
import com.elderlycare.pojo.vo.HealthKnowledgeSearchVO;
import com.elderlycare.pojo.vo.HealthKnowledgeVO;
import com.elderlycare.service.NewsService;
import com.elderlycare.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/health-knowledge")
public class NewsController {

    private final NewsService newsService;
    private final UserMapper userMapper;

    //发布文章
    @PostMapping("/article")
    public Result<Integer> publishArticle(@Valid @RequestBody NewsAddRequest request) {
        Integer creatorId = getCurrentUserId();
        if (creatorId == null) {
            return Result.error("用户未登录");
        }
        return newsService.publishArticle(request, creatorId);
    }


    //修改文章
    @PutMapping("/article/{id}")
    public Result<Void> updateArticle(
            @PathVariable Integer id,
            @RequestBody NewsUpdateRequest request
    ) {
        return newsService.updateArticle(id, request);
    }

    //删除文章
    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable Integer id) {
        return newsService.deleteArticle(id);
    }


    //文章详情
    @GetMapping("/article/{id}")
    public Result<HealthKnowledgeVO> getArticleDetail(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer userId
    ) {
        return newsService.getArticleDetail(id, userId);
    }

    //分类文章
    @GetMapping("/articles")
    public Result<List<HealthKnowledgeVO>> getArticlesByCategory(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer userId
    ) {
        return newsService.getArticlesByCategory(category, userId);
    }

    @GetMapping("/recommended")
    public Result<List<HealthKnowledgeVO>> getRecommendedArticles(
            @RequestParam(required = false) Integer userId
    ) {
        return newsService.getRecommendedArticles(userId);
    }

    //热门文章
    @GetMapping("/popular")
    public Result<List<HealthKnowledgeVO>> getPopularArticles(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Integer userId
    ) {
        return newsService.getPopularArticles(limit, userId);
    }


    //搜索文章
    @GetMapping("/search")
    public Result<HealthKnowledgeSearchVO> searchArticles(
            @RequestParam String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Integer userId = getCurrentUserId();
        return newsService.searchArticles(keyword, category, page, size, userId);
    }


    //文章列表
    @GetMapping("/list")
    public Result<HealthKnowledgeSearchVO> getNewsList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword
    ) {
        Integer userId = getCurrentUserId();
        return newsService.searchArticles(keyword, category, pageNum, pageSize, userId);
    }
    @PostMapping("/collect")
    public Result<Void> collectArticle(@Valid @RequestBody CollectRequest request) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return newsService.collectArticle(request, userId);
    }

    @DeleteMapping("/collect")
    public Result<Void> cancelCollectArticle(@Valid @RequestBody CollectRequest request) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return newsService.cancelCollectArticle(request, userId);
    }

    @GetMapping("/collects")
    public Result<List<HealthKnowledgeCollectVO>> getUserCollects() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return newsService.getUserCollects(userId);
    }

    @GetMapping("/collect/check")
    public Result<Boolean> checkIfCollected(@RequestParam Integer newsId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return newsService.checkIfCollected(newsId, userId);
    }

    /**
     * 点赞资讯
     */
    @PostMapping("/like")
    public Result<Void> likeArticle(@Valid @RequestBody LikeRequest request) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return newsService.likeArticle(request, userId);
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/like")
    public Result<Void> cancelLikeArticle(@Valid @RequestBody LikeRequest request) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return newsService.cancelLikeArticle(request, userId);
    }

    /**
     * 检查是否已点赞
     */
    @GetMapping("/like/check")
    public Result<Boolean> checkIfLiked(@RequestParam Integer newsId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return newsService.checkIfLiked(newsId, userId);
    }

    private Integer getCurrentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                String phone = ((User) principal).getUsername();
                com.elderlycare.pojo.entity.User user = userMapper.selectByPhone(phone);
                return user != null ? user.getId() : null;
            }
        } catch (Exception e) {
            log.warn("获取当前用户 ID 失败：{}", e.getMessage());
        }
        return null;
    }
}

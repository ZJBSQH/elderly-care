package com.elderlycare.controller.admin;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.dto.news.NewsAddRequest;
import com.elderlycare.pojo.dto.news.NewsUpdateRequest;
import com.elderlycare.pojo.vo.admin.AdminNewsVO;
import com.elderlycare.pojo.vo.PageResult;
import com.elderlycare.service.AdminNewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 资讯管理控制器
 */
@RestController
@RequestMapping("/admin/news")
@RequiredArgsConstructor
public class AdminNewsController {

    private final AdminNewsService adminNewsService;

    /**
     * 发布资讯
     */
    @PostMapping
    public Result<Integer> publishNews(@Valid @RequestBody NewsAddRequest request) {
        Integer creatorId = getCurrentUserId();
        return Result.success(adminNewsService.publishNews(request, creatorId));
    }

    /**
     * 更新资讯
     */
    @PutMapping
    public Result<Void> updateNews(@Valid @RequestBody NewsUpdateRequest request) {
        boolean success = adminNewsService.updateNews(request);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 删除资讯
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteNews(@PathVariable Integer id) {
        boolean success = adminNewsService.deleteNews(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    /**
     * 查询资讯列表（分页）
     */
    @GetMapping
    public Result<PageResult<AdminNewsVO>> getNewsList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(adminNewsService.queryNews(page, size, category, status));
    }

    /**
     * 获取资讯详情
     */
    @GetMapping("/{id}")
    public Result<AdminNewsVO> getNewsDetail(@PathVariable Integer id) {
        return Result.success(adminNewsService.getNewsDetail(id));
    }

    /**
     * 上架/下架资讯
     */
    @PutMapping("/{id}/status")
    public Result<Void> changeNewsStatus(
            @PathVariable Integer id,
            @RequestParam Integer status
    ) {
        boolean success = adminNewsService.changeNewsStatus(id, status);
        return success ? Result.success() : Result.error("修改失败");
    }

    /**
     * 推荐/取消推荐资讯
     */
    @PutMapping("/{id}/recommended")
    public Result<Void> changeNewsRecommended(
            @PathVariable Integer id,
            @RequestParam Integer isRecommended
    ) {
        boolean success = adminNewsService.changeNewsRecommended(id, isRecommended);
        return success ? Result.success() : Result.error("修改失败");
    }

    /**
     * 获取当前登录用户 ID（从 JWT Token 中提取）
     * TODO: 实际项目中需要从 JWT token 中解析
     */
    private Integer getCurrentUserId() {
        // 这里暂时返回一个默认值，实际应该从 JWT token 中解析
        // 示例：return (Integer) RequestContextHolder.getRequestAttributes().getAttribute("userId", RequestAttributes.SCOPE_REQUEST);
        return 1; // 测试用
    }
}

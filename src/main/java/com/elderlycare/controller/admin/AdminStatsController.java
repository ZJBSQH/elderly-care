package com.elderlycare.controller.admin;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.vo.admin.DashboardStatsVO;
import com.elderlycare.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据统计管理控制器
 */
@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminUserService adminUserService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard")
    public Result<DashboardStatsVO> getDashboardStats() {
        // 用户统计
        Long totalUsers = adminUserService.countUsers(null);
        Long elderCount = adminUserService.countUsers(0);
        Long familyCount = adminUserService.countUsers(1);
        Long adminCount = adminUserService.countUsers(2);
        
        // TODO: 添加健康数据统计
        Integer todayHealthRecords = 0;
        Integer activeMedicines = 0;
        Integer todayWarnings = 0;
        BigDecimal normalRate = BigDecimal.valueOf(93.5);
        
        // TODO: 添加资讯统计
        Long totalNews = 0L;
        Long publishedNews = 0L;
        
        // TODO: 添加公告统计
        Integer totalAnnouncements = 0;
        
        DashboardStatsVO stats = new DashboardStatsVO(
            totalUsers,
            elderCount,
            familyCount,
            adminCount,
            todayHealthRecords,
            activeMedicines,
            todayWarnings,
            normalRate,
            totalNews,
            publishedNews,
            totalAnnouncements
        );
        
        return Result.success(stats);
    }
    
    /**
     * 获取用户趋势统计
     */
    @GetMapping("/users/trend")
    public Result<Map<String, Object>> getUserTrend(
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        Map<String, Object> trend = new HashMap<>();
        // TODO: 实现用户趋势统计
        return Result.success(trend);
    }
    
    /**
     * 获取健康数据统计
     */
    @GetMapping("/health/stats")
    public Result<Map<String, Object>> getHealthStats(
            @RequestParam(required = false) Integer elderId
    ) {
        Map<String, Object> stats = new HashMap<>();
        // TODO: 实现健康数据统计
        return Result.success(stats);
    }
}

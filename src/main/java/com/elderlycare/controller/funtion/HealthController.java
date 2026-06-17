package com.elderlycare.controller.funtion;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.dto.health.HealthDTO;
import com.elderlycare.pojo.vo.HealthStatisticsVO;
import com.elderlycare.pojo.vo.HealthTrendVO;
import com.elderlycare.pojo.vo.HealthVO;
import com.elderlycare.pojo.vo.HealthWarningVO;
import com.elderlycare.service.HealthService;
import com.elderlycare.service.HealthWarningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康数据管理控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/health")
public class HealthController {
    
    private final HealthService healthService;
    private final HealthWarningService healthWarningService;
    
    /**
     * 录入健康数据（基础版，不触发预警）
     */
    @PostMapping("/record")
    public Result<HealthVO> record(@Valid @RequestBody HealthDTO request) {
        return healthService.saveHealth(request);
    }
    
    /**
     * 录入健康数据并自动预警（预警版）
     */
    @PostMapping("/alert/record")
    public Result<HealthVO> alertRecord(@Valid @RequestBody HealthDTO request) {
        return healthWarningService.saveHealthWithWarning(request);
    }
    
    /**
     * 查询今日记录
     */
    @GetMapping("/today")
    public Result<List<HealthVO>> today(@RequestParam Integer elderId) {
        return healthService.getTodayRecords(elderId);
    }
    
    /**
     * 查询历史记录
     */
    @GetMapping("/history")
    public Result<List<HealthVO>> history(
            @RequestParam Integer elderId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return healthService.getHistoryRecords(elderId, startDate, endDate);
    }
    
    /**
     * 查询最新记录
     */
    @GetMapping("/latest")
    public Result<HealthVO> latest(@RequestParam Integer elderId) {
        return healthService.getLatestRecord(elderId);
    }

    /**
     * 查询健康趋势数据（完整版）
     */
    @GetMapping("/trend")
    public Result<HealthTrendVO> trend(
            @RequestParam Integer elderId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return healthService.getHealthTrend(elderId, startDate, endDate);
    }
    
    /**
     * 查询预警列表
     */
    @GetMapping("/alert/list")
    public Result<List<HealthWarningVO>> alertList(@RequestParam Integer userId) {
        return healthWarningService.getUserWarnings(userId);
    }
    
    /**
     * 查询未读预警数量
     */
    @GetMapping("/alert/unread")
    public Result<Integer> alertUnread(@RequestParam Integer userId) {
        return healthWarningService.countUnreadWarnings(userId);
    }
    
    /**
     * 标记预警为已读
     */
    @PostMapping("/alert/read/{id}")
    public Result<Void> alertMarkAsRead(@PathVariable Integer id) {
        return healthWarningService.markWarningAsRead(id);
    }
    
    /**
     * 标记所有预警为已读
     */
    @PostMapping("/alert/read-all")
    public Result<Void> alertMarkAllAsRead(@RequestParam Integer userId) {
        return healthWarningService.markAllWarningsAsRead(userId);
    }
    
    /**
     * 删除单条健康记录
     */
    @DeleteMapping("/record/{id}")
    public Result<Void> deleteRecord(@PathVariable Integer id) {
        return healthService.deleteRecord(id);
    }
    
    /**
     * 批量删除健康记录（按日期范围）
     */
    @DeleteMapping("/records")
    public Result<Void> deleteRecords(
            @RequestParam Integer elderId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return healthService.deleteRecordsByDateRange(elderId, startDate, endDate);
    }
    
    /**
     * 更新健康记录
     */
    @PutMapping("/record/{id}")
    public Result<HealthVO> updateRecord(
            @PathVariable Integer id,
            @Valid @RequestBody HealthDTO request
    ) {
        return healthService.updateRecord(id, request);
    }
    
    /**
     * 获取健康数据统计报告
     */
    @GetMapping("/statistics")
    public Result<HealthStatisticsVO> statistics(
            @RequestParam Integer elderId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return healthService.getStatistics(elderId, startDate, endDate);
    }
}

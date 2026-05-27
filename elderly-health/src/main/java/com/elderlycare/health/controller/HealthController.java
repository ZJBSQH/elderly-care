package com.elderlycare.health.controller;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.health.dto.HealthDTO;
import com.elderlycare.health.service.HealthService;
import com.elderlycare.health.vo.AlertVO;
import com.elderlycare.health.vo.HealthVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 健康记录控制器
 *
 * @author 郑
 */
@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    /**
     * 保存健康记录
     *
     * @param healthDTO 健康记录DTO
     * @return 保存的健康记录
     */
    @PostMapping("/record")
    public Result<HealthVO> saveHealth(@Valid @RequestBody HealthDTO healthDTO) {
        log.info("保存健康记录请求，elderId: {}", healthDTO.getElderId());
        return healthService.saveHealth(healthDTO);
    }

    /**
     * 获取当天健康记录
     *
     * @param elderId 老人ID
     * @return 当天健康记录列表
     */
    @GetMapping("/today")
    public Result<List<HealthVO>> getTodayRecords(@RequestParam Integer elderId) {
        log.info("查询当天健康记录，elderId: {}", elderId);
        return healthService.getTodayRecords(elderId);
    }

    /**
     * 获取历史健康记录
     *
     * @param elderId 老人ID
     * @param start   开始时间
     * @param end     结束时间
     * @return 历史健康记录列表
     */
    @GetMapping("/history")
    public Result<List<HealthVO>> getHistoryRecords(
            @RequestParam Integer elderId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        log.info("查询历史健康记录，elderId: {}, start: {}, end: {}", elderId, start, end);
        return healthService.getHistoryRecords(elderId, start, end);
    }

    /**
     * 获取最新健康记录
     *
     * @param elderId 老人ID
     * @return 最新健康记录
     */
    @GetMapping("/latest")
    public Result<HealthVO> getLatestRecord(@RequestParam Integer elderId) {
        log.info("查询最新健康记录，elderId: {}", elderId);
        return healthService.getLatestRecord(elderId);
    }

    /**
     * 获取健康统计信息
     *
     * @param elderId 老人ID
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(@RequestParam Integer elderId) {
        log.info("查询健康统计信息，elderId: {}", elderId);
        return healthService.getStatistics(elderId);
    }

    /**
     * 更新健康记录
     *
     * @param id        记录ID
     * @param healthDTO 更新数据
     * @return 更新后的健康记录
     */
    @PutMapping("/record/{id}")
    public Result<HealthVO> updateHealthRecord(@PathVariable Long id, @Valid @RequestBody HealthDTO healthDTO) {
        log.info("更新健康记录请求，id: {}", id);
        return healthService.updateRecord(id, healthDTO);
    }

    /**
     * 删除健康记录
     *
     * @param id 记录ID
     * @return 删除结果
     */
    @DeleteMapping("/record/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        log.info("删除健康记录请求，id: {}", id);
        return healthService.deleteRecord(id);
    }

    /**
     * 获取预警列表
     *
     * @param elderId 老人ID
     * @return 预警列表
     */
    @GetMapping("/alert/list")
    public Result<List<AlertVO>> getAlertList(@RequestParam Integer elderId) {
        log.info("查询预警列表，elderId: {}", elderId);
        return healthService.getAlertList(elderId);
    }

    /**
     * 获取未读预警数量
     *
     * @param elderId 老人ID
     * @return 未读预警数量
     */
    @GetMapping("/alert/unread")
    public Result<Integer> getUnreadAlertCount(@RequestParam Integer elderId) {
        log.info("查询未读预警数量，elderId: {}", elderId);
        return healthService.getUnreadAlertCount(elderId);
    }

    /**
     * 标记预警已读
     *
     * @param id 健康记录ID
     * @return 操作结果
     */
    @PostMapping("/alert/read/{id}")
    public Result<Void> markAlertRead(@PathVariable Long id) {
        log.info("标记预警已读，id: {}", id);
        return healthService.markAlertRead(id);
    }

    /**
     * 标记全部预警已读
     *
     * @param elderId 老人ID
     * @return 操作结果
     */
    @PostMapping("/alert/read-all")
    public Result<Void> markAllAlertsRead(@RequestParam Integer elderId) {
        log.info("标记全部预警已读，elderId: {}", elderId);
        return healthService.markAllAlertsRead(elderId);
    }

    /**
     * 保存健康记录并预警（与 /record 功能相同，保留路径兼容前端调用）
     *
     * @param healthDTO 健康记录DTO
     * @return 保存的健康记录
     */
    @PostMapping("/alert/record")
    public Result<HealthVO> saveHealthWithAlert(@Valid @RequestBody HealthDTO healthDTO) {
        log.info("保存健康记录（预警路径），elderId: {}", healthDTO.getElderId());
        return healthService.saveHealth(healthDTO);
    }
}
